package cn.harryh.arkpets.rpc;

import org.freedesktop.dbus.TypeRef;
import org.freedesktop.dbus.annotations.DBusBoundProperty;
import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.interfaces.Properties;

import java.util.List;


@DBusInterfaceName("org.kde.KWin.Plugins")
public interface KWinPluginInterface extends DBusInterface, Properties {
    boolean LoadPlugin(String name);

    @DBusBoundProperty(type = StringList.class)
    List<String> getLoadedPlugins();

    @DBusBoundProperty(type = StringList.class)
    List<String> getAvailablePlugins();

    interface StringList extends TypeRef<List<String>> {
    }
}
