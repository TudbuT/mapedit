package tudbut.mapedit;

import de.tudbut.tools.FileRW;
import de.tudbut.tools.Tools;
import de.tudbut.type.StringArray;
import tudbut.parsing.AddressedTCN;
import tudbut.parsing.JSON;
import tudbut.parsing.TCN;
import tudbut.parsing.TCNArray;
import tudbut.tools.JButtonList;

import javax.swing.*;
import java.io.IOException;
import java.util.Objects;
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
        boolean array = mapStack.peek().isArray;
        list.addButton(new JButton("Back"), (jButton, jPanel, jButtonList) -> {
            try {
                mapStack.get(mapStack.size() - 2).set(mapKeyStack.pop(), mapStack.pop());
                display(jButtonList);
            } catch (Exception e) {
                save(file, type);
                file = null;
                mapStack.clear();
                mapKeyStack.clear();
                frame.dispose();
                frame.setVisible(false);
                frame = null;
                new Thread(() -> Main.main(null)).start();
            }
        });
        list.addButton(new JButton("Save as"), (jButton, jPanel, jButtonList) -> {
            FileRW file;
            try {
                file = new FileRW(Objects.requireNonNull(file(false)));
            }
            catch (Exception ignored) {
                return;
            }
            jPanel.removeAll();
            jPanel.repaint();
            for (int i = 1 ; i < Type.values().length ; i++) {
                int finalI = i;
                list.addButton(new JButton(Type.values()[i].toString().toLowerCase()), (jButton1, jPanel1, jButtonList1) -> {
                    save(file, Type.values()[finalI]);
                    display(jButtonList);
                });
            }
        });
        list.addButton(new JButton("Add from file"), (jButton, jPanel, jButtonList) -> {
            FileRW file;
            try {
                file = new FileRW(Objects.requireNonNull(file(true)));
            }
            catch (Exception ignored) {
                return;
            }
            jPanel.removeAll();
            jPanel.repaint();
            for (int i = 1 ; i < Type.values().length ; i++) {
                int finalI = i;
                list.addButton(new JButton(Type.values()[i].toString().toLowerCase()), (jButton1, jPanel1, jButtonList1) -> {
                    display(jButtonList);
                    String key;
                    if(array)
                        key = String.valueOf(mapStack.peek().map.size());
                    else
                        key = JOptionPane.showInputDialog("NEW KEY");
                    try {
                        TCN ld;
                        String s = file.getContent().join("\n");
                        switch (Type.values()[finalI]) {
                            case TCN:
                                ld = TCN.read(s);
                                break;
                            case TCNMAP:
                                ld = TCN.readMap(Tools.stringToMap(s));
                                break;
                            case ADDRTCN:
                                ld = AddressedTCN.addressedToNormal(TCN.read(s));
                                break;
                            case ADDRTCNMAP:
                                ld = AddressedTCN.addressedToNormal(TCN.readMap(Tools.stringToMap(s)));
                                break;
                            case JSON:
                            case JSON_READABLE:
                                ld = JSON.read(s);
                                break;
                            default:
                                throw new IllegalStateException("Unexpected value: " + Type.values()[finalI]);
                        }
                        mapStack.peek().set(key, ld);
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(null, "Thats not a " + type + " file!");
                    }
                    display(jButtonList);
                });
            }
        });
        list.addButton(new JButton("New val"), (jButton, jPanel, jButtonList) -> {
            String s;
            if(array)
                s = String.valueOf(mapStack.peek().map.size());
            else
                s = JOptionPane.showInputDialog("NEW KEY");
            if(s == null)
                return;
            mapStack.peek().set(s, "");
            display(jButtonList);
        });
        list.addButton(new JButton("New sub"), (jButton, jPanel, jButtonList) -> {
            String s;
            if(array)
                s = String.valueOf(mapStack.peek().map.size());
            else
                s = JOptionPane.showInputDialog("NEW KEY");
            if(s == null)
                return;
            mapKeyStack.push(s);
            mapStack.push(new TCN());
            mapStack.get(mapStack.size() - 2).set(s, mapStack.peek());
            display(jButtonList);
        });
        list.addButton(new JButton("New array"), (jButton, jPanel, jButtonList) -> {
            String s;
            if(array)
                s = String.valueOf(mapStack.peek().map.size());
            else
                s = JOptionPane.showInputDialog("NEW KEY");
            if(s == null)
                return;
            mapKeyStack.push(s);
            mapStack.push(new TCNArray().toTCN());
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
                if(array)
                    Integer.parseInt(newKey);
                Object o = mapStack.peek().get(key);
                if(o == null)
                    return;
                mapStack.peek().set(newKey, o);
                mapStack.peek().set(key, null);
            } catch (Exception ignored) { }
            display(jButtonList);
        });
        list.addButton(new JButton("Copy"), (jButton, jPanel, jButtonList) -> {
            String key = JOptionPane.showInputDialog("KEY");
            String newKey = JOptionPane.showInputDialog("NEW KEY");
            if(key == null || newKey == null)
                return;
            try {
                if(array)
                    Integer.parseInt(newKey);
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
                list.addButton(new JButton((mapStack.peek().getSub(key).isArray ? "[ARA] " : "[SUB] ") + key), (jButton, jPanel, jButtonList) -> {
                    mapStack.push(mapStack.peek().getSub(key));
                    mapKeyStack.push(key);
                    display(list);
                });
            }
            else if(mapStack.peek().getArray(key) != null) {
                list.addButton(new JButton("[ARA] " + key), (jButton, jPanel, jButtonList) -> {
                    mapStack.push(mapStack.peek().getArray(key).toTCN());
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
    
    private static void save(FileRW file, Type type) {
        TCN toWrite = new TCN();
        if (type == Type.TCN) {
            toWrite = mapStack.peek();
        }
        else if (type == Type.TCNMAP) {
            toWrite = mapStack.peek();
        }
        else if (type == Type.ADDRTCN) {
            toWrite = AddressedTCN.normalToAddressed(mapStack.peek());
        }
        else if (type == Type.ADDRTCNMAP) {
            toWrite = AddressedTCN.normalToAddressed(mapStack.peek());
        }
        else if (type == Type.JSON) {
            toWrite = mapStack.peek();
        }
        else if (type == Type.JSON_READABLE) {
            toWrite = mapStack.peek();
        }
        for (String key : toWrite.map.keys()) {
            TCN.deepConvert(key, toWrite.get(key), toWrite);
        }
        if (type == Type.TCN) {
            try {
                file.setContent(toWrite.toString());
            }
            catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        else if (type == Type.TCNMAP) {
            try {
                file.setContent(Tools.mapToString(toWrite.toMap()));
            }
            catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        else if (type == Type.ADDRTCN) {
            try {
                file.setContent(toWrite.toString());
            }
            catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        else if (type == Type.ADDRTCNMAP) {
            try {
                file.setContent(Tools.mapToString(toWrite.toMap()));
            }
            catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        else if (type == Type.JSON) {
            try {
                file.setContent(JSON.write(toWrite));
            }
            catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        else if (type == Type.JSON_READABLE) {
            try {
                file.setContent(JSON.writeReadable(toWrite));
            }
            catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}
