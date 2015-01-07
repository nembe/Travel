package nl.yellowbrick.admin.dialect;

import org.springframework.stereotype.Component;
import org.thymeleaf.dialect.AbstractDialect;
import org.thymeleaf.processor.IProcessor;

import java.util.HashSet;
import java.util.Set;

@Component
public class BrickwallDialect extends AbstractDialect {

  public String getPrefix() {
    return "bw";
  }

  @Override
  public Set<IProcessor> getProcessors() {
    Set<IProcessor> processors = new HashSet<>();

    processors.add(new ActiveForContextAttrProcessor());

    return processors;
  }
}
