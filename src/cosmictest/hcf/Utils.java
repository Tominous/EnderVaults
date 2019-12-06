/*
 * Copyright (c) 2019. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package cosmictest.hcf;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Utils {

    private String server_version;

    private Method serverHandle;
    private Class serverClass;

    public Utils() {
        try {
            Server server = Bukkit.getServer();
            if (serverHandle == null) {
                serverHandle = server.getClass().getDeclaredMethod("getHandle");
                serverHandle.setAccessible(true);
            }
            Object nmsServer = serverHandle.invoke(server);
            if (serverClass == null) {
                serverClass = nmsServer.getClass();
            }
            String className = serverClass.getPackage().getName();
            String[] args = className.split("\\.");
            server_version = args[3];
         } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String getServerVersion() {
        Validate.notNull(server_version);
        return server_version;
    }

    public class AI {

        private Method getHandle;
        private Method getNBTTag;
        private Class<?> nmsEntityClass;
        private Class<?> nbtTagClass;
        private Method c;
        private Method setInt;
        private Method f;

        public void setAiEnabled(Entity entity, boolean enabled) {

            try {
                if (getHandle == null) {
                    getHandle = entity.getClass().getDeclaredMethod("getHandle");
                    getHandle.setAccessible(true);
                }
                Object nmsEntity = getHandle.invoke(entity);
                if (nmsEntityClass == null) {
                    nmsEntityClass = nmsEntity.getClass();
                }
                if (getNBTTag == null) {
                    getNBTTag = nmsEntityClass.getDeclaredMethod("getNBTTag");
                    getNBTTag.setAccessible(true);
                }
                Object tag = getNBTTag.invoke(nmsEntity);
                if (nbtTagClass == null) {
                    nbtTagClass = tag.getClass();
                }
                if (tag == null) {
                    tag = nbtTagClass.newInstance();
                }
                if (c == null) {
                    c = nmsEntityClass.getDeclaredMethod("c", nbtTagClass);
                    c.setAccessible(true);
                }
                c.invoke(nmsEntity, tag);
                if (setInt == null) {
                    setInt = nbtTagClass.getDeclaredMethod("setInt", String.class, Integer.TYPE);
                    setInt.setAccessible(true);
                }
                int value = enabled ? 0 : 1;
                setInt.invoke(tag, "NoAI", value);
                if (f == null) {
                    f = nmsEntityClass.getDeclaredMethod("f", nbtTagClass);
                    f.setAccessible(true);
                }
                f.invoke(nmsEntity, tag);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public class Visible {

        private Method getHandle;
        private Class nmsEntityClass;
        private Method setInvisible;

        public void setInvisible(Entity entity, boolean invisible) {
            try {
                if (getHandle == null) {
                    getHandle = entity.getClass().getDeclaredMethod("getHandle");
                    getHandle.setAccessible(true);
                }
                Object nmsEntity = getHandle.invoke(entity);
                if (nmsEntityClass == null) {
                    nmsEntityClass = nmsEntity.getClass();
                }
                if (setInvisible == null) {
                    setInvisible = locateMethod(nmsEntityClass,"setInvisible", new Class[] {boolean.class});
                    setInvisible.setAccessible(true);
                }
                setInvisible.invoke(nmsEntity,invisible);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public class Slots {
        private Method getHandle;
        private Class nmsEntityClass;
        private Field slots;

        public void setEnableSlots(Entity entity, boolean usable) {
            if (!(entity instanceof ArmorStand)) {
                throw new IllegalStateException("Enabling slots on non armorstand entity");
            }
            try {
                if (getHandle == null) {
                    getHandle = entity.getClass().getDeclaredMethod("getHandle");
                    getHandle.setAccessible(true);
                }
                Object nmsEntity = getHandle.invoke(entity);
                if (nmsEntityClass == null) {
                    nmsEntityClass = nmsEntity.getClass();
                }
                if (slots == null) {
                    Validate.notNull(server_version, "Trying to decide whether or not entity" +
                            " should have its slots editable whilst server-version is undecided");
                    if (server_version.contains("v1_7_R")) {
                        throw new IllegalStateException("Plugin is not supported by v1_7 and you are using " + server_version);
                    } else if (server_version.equalsIgnoreCase("v1_8_R3")) {
                        slots = locateField(nmsEntityClass,"bi");
                    } else if (server_version.equalsIgnoreCase("v1_13_R1")) {
                        slots = locateField(nmsEntityClass,"bH");
                    } else if (server_version.equalsIgnoreCase("v1_13_R2")) {
                        slots = locateField(nmsEntityClass,"bH");
                    } else if (server_version.equalsIgnoreCase("v1_14_R1")) {
                        slots = locateField(nmsEntityClass, "bE");
                    } else if (server_version.equalsIgnoreCase("v1_11_R1")) {
                        slots = locateField(nmsEntityClass, "bC");
                    } else if (server_version.equalsIgnoreCase("v1_10_R1")) {
                        slots = locateField(nmsEntityClass,"bB");
                    } else if (server_version.equalsIgnoreCase("v1_9_R1")) {
                        slots = locateField(nmsEntityClass,"bz");
                    } else if (server_version.equalsIgnoreCase("v1_9_R3")) {
                        slots = locateField(nmsEntityClass,"bA");
                    } else if (server_version.equalsIgnoreCase("v1_8_R1")) {
                        slots = locateField(nmsEntityClass, "bg");
                    } else if (server_version.equalsIgnoreCase("v1_12_R1")) {
                        slots = locateField(nmsEntityClass, "bB");
                    } else {
                        throw new IllegalStateException("Plugin is not supported by " + server_version);
                    }
                    slots.setAccessible(true);
                }
                slots.set(nmsEntity, usable ? 0 : 0xFFFFFF);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

    public class Invulnerable {
        /*
        Better way of making entites un-killable rather than setting no damage ticks for
         1.8 and lower, class only supports 1.7.10+
         */
        private Method getHandle;
        private Class nmsEntityClass;
        private Field invulnerable;

        public void setInvulnerable(Entity entity, boolean unkillable) {
            try {
                if (getHandle == null) {
                    getHandle = entity.getClass().getDeclaredMethod("getHandle");
                    getHandle.setAccessible(true);
                }
                Object nmsEntity = getHandle.invoke(entity);
                if (nmsEntityClass == null) {
                    nmsEntityClass = nmsEntity.getClass();
                }
                if (invulnerable == null) {
                    invulnerable = locateField(nmsEntityClass, "invulnerable");
                    invulnerable.setAccessible(true);
                }
                invulnerable.set(nmsEntity,unkillable);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public Field locateField(Class<?> clazz, String name) {
        Validate.noNullElements(new Object[] {clazz,name});
        Class<?> current = clazz;
        do {
            try {
                return current.getDeclaredField(name);
            } catch(Exception e) {}
        } while((current = current.getSuperclass()) != null);
        return null;
    }

    public Method locateMethod(Class<?> clazz, String name, Class[] params) {
        Validate.noNullElements(new Object[] {clazz,name});
        Class<?> current = clazz;
        do {
            try {
                return current.getDeclaredMethod(name, params);
            } catch(Exception e) {}
        } while((current = current.getSuperclass()) != null);
        return null;
    }
}
