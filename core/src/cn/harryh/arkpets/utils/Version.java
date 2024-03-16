/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.utils;


/** The class represents a semantic version.
 * <hr>
 * Given a version number {@code MAJOR.MINOR.PATCH}, increment the:
 * <ul>
 *     <li> {@code MAJOR} version when you make incompatible API changes, </li>
 *     <li> {@code MINOR} version when you add functionality in a backward compatible manner, </li>
 *     <li> {@code PATCH} version when you make backward compatible bug fixes. </li>
 * </ul>
 * @see <a href="https://semver.org/">semver.org</a>
 */
public class Version {
    private final int major;
    private final int minor;
    private final int patch;
    private static final int defaultNumber = 0;

    public Version(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public Version(String string) throws IllegalArgumentException {
        String[] array = string.split("\\.");
        major = array.length > 0 ? Integer.parseInt(array[0]) : defaultNumber;
        minor = array.length > 1 ? Integer.parseInt(array[1]) : defaultNumber;
        patch = array.length > 2 ? Integer.parseInt(array[2]) : defaultNumber;
    }

    public Version(int[] array) {
        major = array.length > 0 ? array[0] : defaultNumber;
        minor = array.length > 1 ? array[1] : defaultNumber;
        patch = array.length > 2 ? array[2] : defaultNumber;
    }

    public boolean greaterThan(Version another) {
        if (another == null) return false;
        return (major > another.major) ||
                (major == another.major && minor > another.minor) ||
                (minor == another.minor && patch > another.patch);
    }

    public boolean lessThan(Version another) {
        if (another == null) return false;
        return (major < another.major) ||
                (major == another.major && minor < another.minor) ||
                (minor == another.minor && patch < another.patch);
    }

    public int[] toArray() {
        return new int[] {major, minor, patch};
    }

    @Override
    public String toString() {
        return major + "." + minor + "." + patch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Version that = (Version)o;
        return major == that.major && minor == that.minor && patch == that.patch;
    }

    @Override
    public int hashCode() {
        return (major << 8) + (minor << 4) + patch;
    }
}
