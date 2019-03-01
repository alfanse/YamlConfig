package com.github.jsixface;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YamlConfig {
    private Object content;
    private Pattern arrayKeyPattern = Pattern.compile("^([a-zA-Z][a-zA-Z0-9]+)\\[([0-9]+)]$");

    private YamlConfig() {
    }

    public static YamlConfig load(Reader reader) {
        YamlConfig instance = new YamlConfig();
        Yaml yml = new Yaml();
        instance.content = yml.load(reader);
        return instance;
    }

    public static YamlConfig load(InputStream in) {
        YamlConfig instance = new YamlConfig();
        Yaml yml = new Yaml();
        instance.content = yml.load(in);
        return instance;
    }

    /**
     * Gets the String value for the specified key from the config.
     *
     * @param key Key in dotted notation like <code>first.second[2].third</code>
     * @return  The String value of property. <br ><code>null</code> if the key is not present
     *          or not a leaf node. <code>Boolean</code> or <code>Integer</code> or other format
     *          are converted to String.
     */
    public String getString(String key) {
        Object foundNode = getNode(key, content);
        if (foundNode != null && !(foundNode instanceof Collection)) {
            return foundNode.toString();
        }
        return null;
    }

    public Integer getInt(String key) {
        Object foundNode = getNode(key, content);
        if (foundNode instanceof Integer) {
            return (Integer) foundNode;
        }
        return null;
    }

    private Object getNode(String key, Object foundNode) {
        String[] parts = decompose(key);
        for (String part : parts) {
            int arrayNum = -1;
            Matcher matcher = arrayKeyPattern.matcher(part);
            if (matcher.matches()) {
                part = matcher.group(1);
                arrayNum = Integer.parseInt(matcher.group(2));
            }
            if (foundNode instanceof Map) {
                if (((Map) foundNode).containsKey(part)) {
                    foundNode = ((Map) foundNode).get(part);
                    if (arrayNum >= 0) {
                        if (foundNode instanceof ArrayList
                                && ((ArrayList) foundNode).size() > arrayNum) {
                            foundNode = ((ArrayList) foundNode).get(arrayNum);
                        } else
                            return null;
                    }
                } else
                    return null;
            }
        }
        return foundNode;
    }

    private String[] decompose(String key) {
        return key.split("\\.");
    }
}
