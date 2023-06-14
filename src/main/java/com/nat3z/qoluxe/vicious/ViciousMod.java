package com.nat3z.qoluxe.vicious;

import net.minecraft.client.MinecraftClient;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ViciousMod {
    ViciousConfig config;
    File configFile;
    String name;
    public ViciousMod(ViciousConfig vicious_config, String name) {
        config = vicious_config;
        this.name = name;
        configFile = new File(MinecraftClient.getInstance().runDirectory.getAbsolutePath() + "\\config\\" + name + ".cfg");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public ViciousConfig getConfig() {
        return config;
    }

    public String getName() {
        return name;
    }

    public void saveConfig() {
        Yaml yaml = this.yamlDisplay();
        HashMap<String, Object> data = new HashMap();
        Field[] var3 = this.getConfig().getClass().getDeclaredFields();
        int var4 = var3.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            Field variable = var3[var5];
            if (variable.isAnnotationPresent(Configurable.class)) {
                try {
                    Configurable config = (Configurable)variable.getAnnotation(Configurable.class);
                    List<Object> defined = new ArrayList();
                    defined.add(config.description());
                    if (config.type() == ConfigType.HUD) {
                        HudElement hudElement = (HudElement) variable.get(getConfig());
                        defined.add(hudElement.toList());
                    } else {
                        defined.add(variable.get(getConfig()));
                    }

                    data.put(config.name(), defined);
                } catch (IllegalAccessException var11) {
                    var11.printStackTrace();
                }
            }
        }

        var3 = null;

        try {
            PrintWriter writer = new PrintWriter(this.configFile);
            yaml.dump(data, writer);
        } catch (IOException var10) {
            var10.printStackTrace();
        }
    }

    public void updateConfigVariables() {
        try {
            InputStream inputStream = new FileInputStream(configFile);
            HashMap<String, Object> data = (HashMap<String, Object>) yamlDisplay().load(inputStream);

            if (data == null) return;

            for (Field variable : config.getClass().getDeclaredFields()) {
                if (variable.isAnnotationPresent(Configurable.class)) {
                    Configurable config = variable.getAnnotation(Configurable.class);

                    if (data.containsKey(config.name()) && config.type() == ConfigType.HUD)
                        variable.set(config, new HudElement((List<Integer>)((List<Object>)data.get(config.name())).get(1)));
                    else if (data.containsKey(config.name()))
                        variable.set(config, ((List<Object>)data.get(config.name())).get(1));
                }
            }
        } catch (FileNotFoundException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public List<ConfigItem> getConfigItems() {
        List<ConfigItem> configItemList = new ArrayList<>();
        Field[] var2 = this.getConfig().getClass().getDeclaredFields();
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            Field variable = var2[var4];
            if (variable.isAnnotationPresent(Configurable.class)) {
                Configurable cfg = variable.getAnnotation(Configurable.class);

                configItemList.add(new ConfigItem(variable, cfg.name(), cfg.description(), cfg.subCategory(), cfg.category(), cfg.type(), cfg.UAYOR(), cfg.sliderChoices(), getConfig()));
            }
        }

        return configItemList;
    }

    public Yaml yamlDisplay() {
        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        return new Yaml(options);
    }
}
