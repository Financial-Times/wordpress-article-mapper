package com.ft.wordpressarticletransformer.component;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.jruby.embed.jsr223.JRubyEngineFactory;

import com.google.common.io.Files;

public class ErbTemplatingHelper {
  public static class MockHiera {
    private Map<String, Object> config;

    MockHiera(Map<String, Object> config) {
      this.config = config;
    }

    // scope.function_hiera('foo')
    // scope.function_hiera(['bar',''])

    public Object function_hiera(String[] in) {
      return config.containsKey(in[0]) ? config.get(in[0]) : in[1];
    }
  }

  private static final ScriptEngine JRUBY;

  static {
    // required to propagate the scope object into the ERB template bindings
    // http://stackoverflow.com/questions/10786812/jruby-jsr223-interface-ignores-binidngs [sic]
    System.setProperty("org.jruby.embed.localvariable.behavior", "persistent");

    ScriptEngineManager mgr = new ScriptEngineManager();
    mgr.registerEngineExtension("jruby", new JRubyEngineFactory());

    JRUBY = mgr.getEngineByName("jruby");
  }

  public static void generateConfigFile(String templateFile, Map<String, Object> hieraData,
      String outputFile) throws ScriptException, IOException {

    Bindings bindings = new SimpleBindings();
    bindings.put("scope", new MockHiera(hieraData));

    /*
     * `binding` returns all variables declared in this scope, so they can be used in templates
     */
    String erb = "require 'erb'\n" + "ERB.new(File.read('%s')).result(binding)\n";

    String script = String.format(erb, templateFile);

    String gen = (String) JRUBY.eval(script, bindings);
    Files.write(gen, new File(outputFile), UTF_8);
  }
}
