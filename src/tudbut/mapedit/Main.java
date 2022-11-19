package tudbut.mapedit;

import de.tudbut.tools.FileRW;
import de.tudbut.tools.Tools;
import de.tudbut.type.StringArray;
import tudbut.parsing.AddressedTCN;
import tudbut.parsing.TCN;
import tudbut.tools.JButtonList;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;

public class Main {
    private static final Stack<String> mapKeyStack = new Stack<>();
    private static final Stack<Map<String, String>> mapStack = new Stack<>();
    static FileRW file;
    static JFrame frame;
    static JPanel panel;

    static Type type;
    static JButtonList list;

    enum Type {
        MAP,
        TCN,
        TCNMAP,
        ADDRTCN,
        ADDRTCNMAP,
        JSON,
        JSON_READABLE,
    }

    static String file(boolean b) {
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

    public static void openFileANY(String name) {
        try {
            String extension = name.substring(name.lastIndexOf(".") + 1);
            openFile(name, extension);
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Failed to open this file. Make sure it is compatible!");
            System.exit(1);
        }
    }

    public static void openFile(String name, String type) {
        Type theType = null;
        for (int i = 0; i < Type.values().length; i++) {
            if(Type.values()[i].name().equalsIgnoreCase(type)) {
                theType = Type.values()[i];
            }
        }
        if(theType == null) {
            JOptionPane.showMessageDialog(null, "Failed to open this file. Make sure it is compatible!");
            System.exit(1);
        }
        else {
            try {
                if (theType != Type.MAP) {
                    if (!TCNFormats.go(theType, name))
                        System.exit(1);
                } else {
                    mapStack.push(load((file = new FileRW(name)).getContent().join("\n"), false));
                    mapKeyStack.push("");
                    if (mapStack.peek() == null) {
                        JOptionPane.showMessageDialog(null, "Thats not a map!");
                        return;
                    }
                    panel.removeAll();
                    panel.repaint();
                    display(list, mapStack.peek());
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Failed to open this file. Make sure it is compatible!");
                System.exit(1);
            }
        }
    }

    public static void main(String[] args) {
        frame = new JFrame("MapEdit v3.4.1");
        try {
            frame.setIconImage(ImageIO.read(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("icon.png"))));
        } catch (Exception e) {
            System.out.println("Failed to load icon.png");
        }
        list = new JButtonList(frame);

        panel = list.pane;

        if(args.length == 1) {
            openFileANY(args[0]);
        }
        else if(args.length == 2) {
            openFile(args[0], args[1]);
        }
        else {

            list.addButton(new JButton("Load Map"), (jButton, jPanel, jButtonList) -> {
                panel = jPanel;
                type = Type.MAP;
                String mapFile = file(true);
                try {
                    if (mapFile == null)
                        throw new NullPointerException();
                    mapStack.push(load((file = new FileRW(mapFile)).getContent().join("\n"), false));
                    mapKeyStack.push("");
                    if (mapStack.peek() == null) {
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
                panel = jPanel;
                TCNFormats.go(Type.TCNMAP);
            });
            list.addButton(new JButton("Load TCN"), (jButton, jPanel, jButtonList) -> {
                panel = jPanel;
                TCNFormats.go(Type.TCN);
            });
            list.addButton(new JButton("Load AddressedTCN"), (jButton, jPanel, jButtonList) -> {
                panel = jPanel;
                TCNFormats.go(Type.ADDRTCN);
            });
            list.addButton(new JButton("Load AddressedTCNMAP"), (jButton, jPanel, jButtonList) -> {
                panel = jPanel;
                TCNFormats.go(Type.ADDRTCNMAP);
            });
            list.addButton(new JButton("Load JSON"), (jButton, jPanel, jButtonList) -> {
                panel = jPanel;
                TCNFormats.go(Type.JSON);
            });
            list.addButton(new JButton("Load JSONReadable"), (jButton, jPanel, jButtonList) -> {
                panel = jPanel;
                TCNFormats.go(Type.JSON_READABLE);
            });

            list.pane.add(Box.createRigidArea(new Dimension(0, 10)));

            list.addButton(new JButton("Create Map"), (jButton, jPanel, jButtonList) -> {
                panel = jPanel;
                type = Type.MAP;
                create(jPanel, list);
            });
            list.addButton(new JButton("Create TCNMap"), (jButton, jPanel, jButtonList) -> {
                panel = jPanel;
                TCNFormats.create(Type.TCNMAP);
            });
            list.addButton(new JButton("Create TCN"), (jButton, jPanel, jButtonList) -> {
                panel = jPanel;
                TCNFormats.create(Type.TCN);
            });
            list.addButton(new JButton("Create AddressedTCN"), (jButton, jPanel, jButtonList) -> {
                panel = jPanel;
                TCNFormats.create(Type.ADDRTCN);
            });
            list.addButton(new JButton("Create AddressedTCNMAP"), (jButton, jPanel, jButtonList) -> {
                panel = jPanel;
                TCNFormats.create(Type.ADDRTCNMAP);
            });
            list.addButton(new JButton("Create JSON"), (jButton, jPanel, jButtonList) -> {
                panel = jPanel;
                TCNFormats.create(Type.JSON);
            });
            list.addButton(new JButton("Create JSONReadable"), (jButton, jPanel, jButtonList) -> {
                panel = jPanel;
                TCNFormats.create(Type.JSON_READABLE);
            });
        }
        frame.setSize(500,500);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private static void create(JPanel jPanel, JButtonList list) {
        String mapFile = file(false);
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
        if(type == Type.TCN || type == Type.TCNMAP)
            return val.replaceAll("%C", ":").replaceAll("%P", "%");
        return val;
    }

    private static String sval(String val) {
        if(type == Type.TCN || type == Type.TCNMAP)
            return val.replaceAll("%", "%P").replaceAll(":", "%C");
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
                new Thread(() -> main(new String[0])).start();
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
