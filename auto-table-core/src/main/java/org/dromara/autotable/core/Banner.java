package org.dromara.autotable.core;

import org.dromara.autotable.core.constants.Version;

/**
 * 启动时打印的banner
 * @author don
 */
public class Banner {

    public static void print() {
        System.out.println(
                        "    _         _          _____     _     _      \n" +
                        "   / \\  _   _| |_ ___   |_   _|_ _| |__ | | ___ \n" +
                        "  / _ \\| | | | __/ _ \\    | |/ _` | '_ \\| |/ _ \\\n" +
                        " / ___ \\ |_| | || (_) |   | | (_| | |_) | |  __/\n" +
                        "/_/   \\_\\__,_|\\__\\___/    |_|\\__,_|_.__/|_|\\___|\n" +
                        ":: https://autotable.tangzc.com ::      (v" + Version.VALUE + ")\n");
    }
}
