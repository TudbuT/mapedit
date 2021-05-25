package tudbut.mapedit;

import de.tudbut.tools.FileRW;
import de.tudbut.tools.Tools;
import de.tudbut.type.StringArray;
import tudbut.parsing.AddressedTCN;
import tudbut.parsing.JSON;
import tudbut.parsing.TCN;
import tudbut.tools.JButtonList;

import javax.swing.*;
import java.io.IOException;
import java.util.Stack;

import static tudbut.mapedit.Main.*;

public class TCNFormats {
    private static final Stack<String> mapKeyStack = new Stack<>();
    private static final Stack<TCN> mapStack = new Stack<>();
    
    public static void go(Type type) {
        try {
            String file = file(true);
            if (file == null)
                return;
            String s = (Main.file = new FileRW(file)).getContent().join("\n");
            Main.type = type;
            switch (type) {
                case TCN:
                    mapStack.push(TCN.read(s));
                    break;
                case TCNMAP:
                    mapStack.push(TCN.readMap(Tools.stringToMap(s)));
                    break;
                case ADDRTCN:
                    mapStack.push(AddressedTCN.addressedToNormal(TCN.read(s)));
                    break;
                case ADDRTCNMAP:
                    mapStack.push(AddressedTCN.addressedToNormal(TCN.readMap(Tools.stringToMap(s))));
                    break;
                case JSON:
                    mapStack.push(JSON.read(s));
                    break;
                case JSON_READABLE:
                    mapStack.push(JSON.read(s));
                    break;
            }
            mapKeyStack.push("");
            panel.removeAll();
            panel.repaint();
            display(list);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Thats not a " + type + " file!");
        }
    }
    
    public static void create(Type type) {
        Main.type = type;
        create(panel, list);
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
        mapStack.push(new TCN());
        mapKeyStack.push("");
        display(list);
    }
    
    private static void display(JButtonList list) {
        list.pane.removeAll();
        list.pane.repaint();
        list.addButton(new JButton("Back"), (jButton, jPanel, jButtonList) -> {
            try {
                mapStack.get(mapStack.size() - 2).set(mapKeyStack.pop(), mapStack.pop());
                display(jButtonList);
            } catch (Exception e) {
                if (type == Type.TCN) {
                    try {
                        file.setContent(mapStack.get(0).toString());
                    }
                    catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
                else if(type == Type.TCNMAP) {
                    try {
                        file.setContent(Tools.mapToString(mapStack.get(0).toMap()));
                    }
                    catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
                else if(type == Type.ADDRTCN) {
                    try {
                        file.setContent(AddressedTCN.normalToAddressed(mapStack.get(0)).toString());
                    }
                    catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
                else if(type == Type.ADDRTCNMAP) {
                    try {
                        file.setContent(Tools.mapToString(AddressedTCN.normalToAddressed(mapStack.get(0)).toMap()));
                    }
                    catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
                else if(type == Type.JSON) {
                    try {
                        file.setContent(JSON.write(mapStack.get(0)));
                    }
                    catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
                else if(type == Type.JSON_READABLE) {
                    try {
                        file.setContent(JSON.writeReadable(mapStack.get(0)));
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
                new Thread(() -> Main.main(null)).start();
            }
        });
        list.addButton(new JButton("New val"), (jButton, jPanel, jButtonList) -> {
            String s = JOptionPane.showInputDialog("NEW KEY");
            if(s == null)
                return;
            mapStack.peek().set(s, "");
            display(jButtonList);
        });
        list.addButton(new JButton("New sub"), (jButton, jPanel, jButtonList) -> {
            String s = JOptionPane.showInputDialog("NEW KEY");
            if(s == null)
                return;
            mapKeyStack.push(s);
            mapStack.push(new TCN());
            mapStack.get(mapStack.size() - 2).set(s, mapStack.peek());
            display(jButtonList);
        });
        list.addButton(new JButton("Delete"), (jButton, jPanel, jButtonList) -> {
            String s = JOptionPane.showInputDialog("KEY");
            if(s == null)
                return;
            mapStack.peek().set(s, null);
            display(jButtonList);
        });
        list.addButton(new JButton("Move"), (jButton, jPanel, jButtonList) -> {
            String key = JOptionPane.showInputDialog("KEY");
            String newKey = JOptionPane.showInputDialog("NEW KEY");
            if(key == null || newKey == null)
                return;
            try {
                Object o = mapStack.peek().get(key);
                if(o == null)
                    return;
                mapStack.peek().set(newKey, o);
                mapStack.peek().set(key, null);
            } catch (NullPointerException ignored) { }
            display(jButtonList);
        });
        list.addButton(new JButton("Copy"), (jButton, jPanel, jButtonList) -> {
            String key = JOptionPane.showInputDialog("KEY");
            String newKey = JOptionPane.showInputDialog("NEW KEY");
            if(key == null || newKey == null)
                return;
            try {
                Object o = mapStack.peek().get(key);
                if(o == null)
                    return;
                mapStack.peek().set(newKey, o);
            } catch (NullPointerException ignored) { }
            display(jButtonList);
        });
        list.addButton(new JButton(new StringArray(mapKeyStack.toArray(new String[0])).join("/") + "/"), (jButton, jPanel, jButtonList) -> {});
    
        for (String key : mapStack.peek().map.keys()) {
            if(mapStack.peek().getSub(key) != null) {
                list.addButton(new JButton("[SUB] " + key), (jButton, jPanel, jButtonList) -> {
                    mapStack.push(mapStack.peek().getSub(key));
                    mapKeyStack.push(key);
                    display(list);
                });
            }
            else {
                list.addButton(new JButton("[VAL] " + key + ": " + mapStack.peek().getString(key)), (jButton, jPanel, jButtonList) -> {
                    String s = JOptionPane.showInputDialog("NEW VAL:", mapStack.peek().getString(key));
                    if(s != null)
                        mapStack.peek().set(key, s);
                    display(list);
                });
            }
        }
    }
}