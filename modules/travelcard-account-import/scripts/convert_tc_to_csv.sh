#!/bin/bash

read -d '' PARSER << 'EOF'
function format_license_plate(lp) {
  lp = toupper(lp)
  gsub(/[0-9]+|[A-Z]+/, "&-", lp) # insert dashes
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
  echo "Usage: convert.sh ./in/*.txt ./out"
  exit 1
fi

in=(${@:0:$#}) # all except last argument
out=${*: -1} # last argument

for file in "${in[@]}"
do
  csv=$(awk "$PARSER" $file)
  if [ $? -eq 0 ]; 
  then
    filename="$out/$(basename $file).csv"
    echo "$csv" > $filename
    echo "wrote converted $filename"
    donefilename="$file.done"
    mv $file $donefilename
    echo "moved original $donefilename"
  else
    echo "parse error or incomplete file: $file"
  fi
done
