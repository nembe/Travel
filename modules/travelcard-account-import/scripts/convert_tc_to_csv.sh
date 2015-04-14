#!/bin/bash

read -d '' PARSER << 'EOF'
function format_license_plate(lp) {
  lp = toupper(lp)
  gsub(/[0-9]+|[A-Z]+/, "&-", lp) # insert dashes when switching between numeric and non-numeric

  # split group of 4 non-numeric characters
  # unfortunately regular awk's regex doesnt support lookahead/lookbehind
  # so we use this match to work around that
  if(match(lp, /[A-Z]{4}/) > 0) {
    sub(/[A-Z]{2}/, "&-", lp)
  }

  gsub(/-$/, "", lp) # remove trailing dash

  return lp
}

function format_tc_number(tc) {
  gsub(/[^ 0-9A-Za-z]/, "", tc) # strip out non alphanumeric and white spaces
  return tc
}

BEGIN {
  records = 0
}

/^1/ {
  records = records + 1 # count records in file

  license_plate = format_license_plate(substr($1, 2)) # format license plate (ignoring the "1" prefix)
  tc_number = format_tc_number($2)

  print tc_number "," license_plate
}

/^9/ {
  expected_records = substr($1, 2) + 0 # get expected record count (ignoring the "9" prefix)
  expected_records = expected_records + 0 # force conversion to decimal
}

END {
  if(expected_records != records) {
    # exit code != 0 to signal error
    exit 1
  }
}
EOF

if [ -z "$1" ];
then
  echo "Usage: convert.sh ./out"
  exit 1
fi

# SFTP config
HOST=sftp.tmcit.nl
USER=trvlcrdusr
PASS=1wtuybn
PORT=2022
PICKUP_DIR=/files/whitelist
DROPOFF_DIR=/files/whitelist/backup

function do_in_sftp {
  lftp -u ${USER},${PASS} -p ${PORT} sftp://${HOST} $1
}

function copy_files_from_sftp {
  pushd tmp
  do_in_sftp <<EOF
    mget $PICKUP_DIR/*.txt
    bye
EOF
  popd
}

function move_done_file_sftp {
  echo "moving $1 to dropoff directory"

  do_in_sftp <<EOF
    mv $PICKUP_DIR/$1 $DROPOFF_DIR/$1
EOF
}

function younger_than_last_processed {
  ! [ -a .processed ] && return 0 # no .processed file yet
  last_processed=$(cat .processed | head -n 1)
  [ $1 = $(echo -e "$1\n$last_processed" | sort -V | tail -n 1) ] && return 0 || return 1
}

function update_last_processed {
  echo "$1" > .processed
}

function notify_outdated_file {
  echo "Skipping file $1" | mail -s "[TCIMPORT] outdated whitelist file" beheer@brickparking.com
}

out=${*: -1} # last argument

copy_files_from_sftp

for file in `ls -v tmp/*.txt`
do
  csv=$(awk "$PARSER" $file)
  if [ $? -eq 0 ];
  then
    basename=$(basename $file)
    if younger_than_last_processed $basename;
    then
        filename="work/$basename.csv"
        echo "$csv" > $filename
        echo "wrote converted $filename"
        mv $filename $out
        move_done_file_sftp $basename
        update_last_processed $basename
    else
        echo "skipping outdated file: $basename"
        notify_outdated_file $basename
    fi
  else
    echo "parse error or incomplete file: $file"
  fi
done

rm tmp/*

