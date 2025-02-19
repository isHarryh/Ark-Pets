package cn.harryh.arkpets.natives;

import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.types.Variant;

import java.util.Map;


@DBusInterfaceName("org.gnome.Shell.Extensions")
public interface MutterPluginInterface extends DBusInterface {
    boolean EnableExtension(String uuid);

    Map<String, Variant<?>> GetExtensionInfo(String uuid);
}
