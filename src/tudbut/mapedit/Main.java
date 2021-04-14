package tudbut.mapedit;

import de.tudbut.tools.FileRW;
import de.tudbut.tools.Keyboard;
import de.tudbut.tools.Tools;
import de.tudbut.type.StringArray;
import tudbut.parsing.TCN;
import tudbut.tools.JButtonList;

import javax.swing.*;
import javax.swing.filechooser.FileView;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class Main {
    private static final Stack<String> mapKeyStack = new Stack<>();
    private static final Stack<Map<String, String>> mapStack = new Stack<>();
    private static FileRW file;
    private static JFrame frame;
    
    private enum Type {
        TCN,
        MAP,
        TCNMAP,
    }
    
    private static Type type;
    
    private static String file(boolean b) {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File(".").getAbsoluteFile());
        chooser.setMultiSelectionEnabled(false);
        Action details = chooser.getActionMap().get("viewTypeDetails");
        details.actionPerformed(null);
        if(b) {
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                return chooser.getSelectedFile().getAbsolutePath();
            }
        }
        else {
            if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                return chooser.getSelectedFile().getAbsolutePath();
            }
        }
        return null;
    }
    
    public static void main(String[] args) {
        frame = new JFrame("MapEdit v2.1.1");
        JButtonList list = new JButtonList(frame);
        
        list.addButton(new JButton("Load Map"), (jButton, jPanel, jButtonList) -> {
            type = Type.MAP;
            String mapFile = file(true); //JOptionPane.showInputDialog("Please input a map file: ")
            try {
                if(mapFile == null)
                    throw new NullPointerException();
                mapStack.push(load((file = new FileRW(mapFile)).getContent().join("\n"), false));
                mapKeyStack.push("");
                if(mapStack.peek() == null) {
                    JOptionPane.showMessageDialog(null, "Thats not a map!");
                    return;
                }
                jPanel.removeAll();
                jPanel.repaint();
                display(list, mapStack.peek());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Thats not a map file!");
            }
        });
        list.addButton(new JButton("Load TCNMap"), (jButton, jPanel, jButtonList) -> {
            type = Type.TCNMAP;
            String mapFile = file(true); //JOptionPane.showInputDialog("Please input a TCNMap file: ")
            try {
                if(mapFile == null)
                    throw new NullPointerException();
                mapStack.push(load((file = new FileRW(mapFile)).getContent().join("\n"), false));
                mapKeyStack.push("");
                if(mapStack.peek() == null) {
                    JOptionPane.showMessageDialog(null, "Thats not a TCNMap!");
                    return;
                }
                jPanel.removeAll();
                jPanel.repaint();
                display(list, mapStack.peek());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Thats not a TCNMap file!");
            }
        });
        list.addButton(new JButton("Load TCN"), (jButton, jPanel, jButtonList) -> {
            type = Type.TCN;
            String mapFile = file(true); //JOptionPane.showInputDialog("Please input a TCN file: ")
            try {
                if(mapFile == null)
                    throw new NullPointerException();
                mapStack.push(load((file = new FileRW(mapFile)).getContent().join("\n"), false));
                mapKeyStack.push("");
                if(mapStack.peek() == null) {
                    JOptionPane.showMessageDialog(null, "Thats not a TCN!");
                    return;
                }
                jPanel.removeAll();
                jPanel.repaint();
                display(list, mapStack.peek());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Thats not a TCN file!");
            }
        });
    
        list.addButton(new JButton("Create Map"), (jButton, jPanel, jButtonList) -> {
            type = Type.MAP;
            create(jPanel, list);
        });
        list.addButton(new JButton("Create TCNMap"), (jButton, jPanel, jButtonList) -> {
            type = Type.TCNMAP;
            create(jPanel, list);
        });
        list.addButton(new JButton("Create TCN"), (jButton, jPanel, jButtonList) -> {
            type = Type.TCN;
            create(jPanel, list);
        });
        frame.setSize(500,500);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    private static void create(JPanel jPanel, JButtonList list) {
        String mapFile = file(false); // JOptionPane.showInputDialog("Please input a file: ")
        if(mapFile == null)
            return;
        jPanel.removeAll();
        jPanel.repaint();
        try {
            file = new FileRW(mapFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mapStack.push(new HashMap<>());
        mapKeyStack.push("");
        display(list, mapStack.peek());
    }
    
    private static void display(JButtonList list, Map<String, String> map) {
        list.pane.removeAll();
        update(list);
        for (String key : map.keySet()) {
            if(load(map.get(key), true) != null) {
                list.addButton(new JButton("[SUB] " + key), (jButton, jPanel, jButtonList) -> {
                    mapStack.push(load(map.get(key), true));
                    mapKeyStack.push(key);
                    display(list, mapStack.peek());
                });
            }
            else {
                list.addButton(new JButton("[VAL] " + key + ": " + val(map.get(key))), (jButton, jPanel, jButtonList) -> {
                    String s = JOptionPane.showInputDialog("NEW VAL:", val(map.get(key)));
                    if(s != null)
                        map.put(key, sval(s));
                    display(list, map);
                });
            }
        }
    }
    
    private static String val(String val) {
        switch (type) {
            case TCNMAP:
            case TCN:
                return val.replaceAll("%C", ":").replaceAll("%P", "%");
        }
        return val;
    }
    
    private static String sval(String val) {
        switch (type) {
            case TCNMAP:
            case TCN:
                return val.replaceAll("%", "%P").replaceAll(":", "%C");
        }
        return val;
    }
    
    private static Map<String, String> load(String map, boolean b) {
        if(b) {
            try {
                Map<String, String> m = Tools.stringToMap(map);
                return m.isEmpty() ? null : m;
            }
            catch (Exception e) {
                return null;
            }
        }
        switch (type) {
            case MAP:
            case TCNMAP:
                try {
                    Map<String, String> m = Tools.stringToMap(map);
                    return m.isEmpty() ? null : m;
                } catch (Exception e) {
                    return null;
                }
            case TCN:
                try {
                    TCN m = TCN.read(map);
                    return m.map.size() == 0 ? null : m.toMap();
                } catch (Exception e) {
                    return null;
                }
        }
        return null;
    }
    
    private static void update(JButtonList list) {
        list.addButton(new JButton("Back"), (jButton, jPanel, jButtonList) -> {
            try {
                mapStack.get(mapStack.size() - 2).put(mapKeyStack.pop(), Tools.mapToString(mapStack.pop()));
                display(jButtonList, mapStack.peek());
            } catch (Exception e) {
                if (type == Type.TCN) {
                    try {
                        file.setContent(TCN.readMap(mapStack.get(0)).toString());
                    }
                    catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
                else {
                    try {
                        file.setContent(Tools.mapToString(mapStack.get(0)));
                    }
                    catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
                file = null;
                mapStack.clear();
                mapKeyStack.clear();
                frame.dispose();
                frame.setVisible(false);
                frame = null;
                new Thread(() -> main(null)).start();
            }
        });
        list.addButton(new JButton("New val"), (jButton, jPanel, jButtonList) -> {
            String s = JOptionPane.showInputDialog("NEW KEY");
            if(s == null)
                return;
            mapStack.peek().put(s, "");
            display(jButtonList, mapStack.peek());
        });
        list.addButton(new JButton("New sub"), (jButton, jPanel, jButtonList) -> {
            String s = JOptionPane.showInputDialog("NEW KEY");
            if(s == null)
                return;
            mapKeyStack.push(s);
            mapStack.push(new HashMap<>());
            display(jButtonList, mapStack.peek());
        });
        list.addButton(new JButton("Delete"), (jButton, jPanel, jButtonList) -> {
            String s = JOptionPane.showInputDialog("KEY");
            if(s == null)
                return;
            mapStack.peek().remove(s);
            display(jButtonList, mapStack.peek());
        });
        list.addButton(new JButton("Move"), (jButton, jPanel, jButtonList) -> {
            String key = JOptionPane.showInputDialog("KEY");
            String newKey = JOptionPane.showInputDialog("NEW KEY");
            if(key == null || newKey == null)
                return;
            try {
                String s = mapStack.peek().get(key);
                if(s == null)
                    return;
                mapStack.peek().put(newKey, s);
                mapStack.peek().remove(key);
            } catch (NullPointerException ignored) { }
            display(jButtonList, mapStack.peek());
        });
        list.addButton(new JButton("Copy"), (jButton, jPanel, jButtonList) -> {
            String key = JOptionPane.showInputDialog("KEY");
            String newKey = JOptionPane.showInputDialog("NEW KEY");
            if(key == null || newKey == null)
                return;
            try {
                String s = mapStack.peek().get(key);
                if(s == null)
                    return;
                mapStack.peek().put(newKey, s);
            } catch (NullPointerException ignored) { }
            display(jButtonList, mapStack.peek());
        });
        list.addButton(new JButton(new StringArray(mapKeyStack.toArray(new String[0])).join("/") + "/"), (jButton, jPanel, jButtonList) -> {});
    }
}
