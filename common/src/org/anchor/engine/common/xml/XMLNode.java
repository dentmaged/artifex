package org.anchor.engine.common.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XMLNode {

    private String name;
    private Map<String, String> attributes;
    private String data;
    private Map<String, List<XMLNode>> childNodes;

    protected XMLNode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getData() {
        return data;
    }

    public String getAttribute(String attr) {
        if (attributes != null) {
            return attributes.get(attr);
        } else {
            return null;
        }
    }

    public XMLNode getChild(String childName) {
        if (childNodes != null) {
            List<XMLNode> nodes = childNodes.get(childName);
            if (nodes != null && !nodes.isEmpty()) {
                return nodes.get(0);
            }
        }
        return null;

    }

    public XMLNode getChildWithAttribute(String childName, String attr, String value) {
        List<XMLNode> children = getChildren(childName);
        if (children == null || children.isEmpty()) {
            return null;
        }
        for (XMLNode child : children) {
            String val = child.getAttribute(attr);
            if (value.equals(val)) {
                return child;
            }
        }
        return null;
    }

    public List<XMLNode> getChildren(String name) {
        if (childNodes != null) {
            List<XMLNode> children = childNodes.get(name);
            if (children != null) {
                return children;
            }
        }
        return new ArrayList<XMLNode>();
    }

    protected void addAttribute(String attr, String value) {
        if (attributes == null) {
            attributes = new HashMap<String, String>();
        }
        attributes.put(attr, value);
    }

    protected void addChild(XMLNode child) {
        if (childNodes == null) {
            childNodes = new HashMap<String, List<XMLNode>>();
        }
        List<XMLNode> list = childNodes.get(child.name);
        if (list == null) {
            list = new ArrayList<XMLNode>();
            childNodes.put(child.name, list);
        }
        list.add(child);
    }

    protected void setData(String data) {
        this.data = data;
    }

}
