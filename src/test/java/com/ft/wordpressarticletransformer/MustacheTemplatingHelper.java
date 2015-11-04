package com.ft.wordpressarticletransformer;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;


public class MustacheTemplatingHelper {
    private static final Mustache.Compiler MUSTACHE = Mustache.compiler().defaultValue("");
    
    public static void generateConfigFile(String templateFile, Map<String,Object> hieraData, String outputFile)
            throws IOException {
        
        try (Reader in = new FileReader(templateFile);
                Writer out = new FileWriter(outputFile)) {
               
               Template t = MUSTACHE.compile(in);
               t.execute(hieraData, out);
           }
    }
}
