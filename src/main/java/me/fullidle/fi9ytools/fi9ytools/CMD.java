package me.fullidle.fi9ytools.fi9ytools;

import me.fullidle.fi9ytools.fi9ytools.data.FI9yData;
import me.fullidle.fi9ytools.fi9ytools.mc9y.Mc9yApi;
import me.fullidle.fi9ytools.fi9ytools.mc9y.post.Post;
import me.fullidle.fi9ytools.fi9ytools.mc9y.post.ResourcePost;
import me.fullidle.fi9ytools.fi9ytools.util.SomeMethod;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CMD implements CommandExecutor, TabCompleter {
    public static ArrayList<String> CMD_LIST = new ArrayList<>(Arrays.asList(
            "search",
            "install",
            "latestresources",
            "reload",
            "showcache",
            "help"));

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage("§c非op不可用!");
            return false;
        }
        if (args.length >= 1&&CMD_LIST.contains(args[0])){
            switch (args[0]){
                case "help":{
                    break;
                }
                case "search":{
                    FI9yData.searchCache.clear();
                    if (args.length < 2){
                        return false;
                    }
                    /*-->搜索<--*/
                    {
                        sender.sendMessage("§a开始搜索ing...");
                        String key = args[1];
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                Post[] search = Mc9yApi.search(key, "", false);
                                cacheSearchData(search);
                                /*-->提示<--*/
                                SomeMethod.showCache(sender);
                                if (search.length > 1){
                                    sender.sendMessage("§a搜索完成");
                                }else{
                                    sender.sendMessage("§c搜索内容是空的(这或许就是没有内容/搜索失败)");
                                }
                            }
                        }.runTaskAsynchronously(FI9yData.plugin);
                    }
                    return false;
                }
                case "install":{
                    if (args.length < 2){
                        return false;
                    }
                    int number = Integer.parseInt(args[1]);
                    Post post = FI9yData.searchCache.get(number);
                    if (!(post instanceof ResourcePost)){
                        sender.sendMessage("§cBukkit标签的资源文件不能下载!");
                        return false;
                    }
                    /*-->下载<--*/
                    ResourcePost resourcePost = (ResourcePost) post;
                    String downloadLink = resourcePost.getDownloadLink();
                    boolean isBukkitPlugin = resourcePost.isBukkitPlugin();
                    if (!isBukkitPlugin){
                        sender.sendMessage("§c非Bukkit标签的资源没办法安装!(自己去下)");
                        sender.sendMessage("§c帖子地址:§3"+downloadLink);
                        return false;
                    }
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            sender.sendMessage("§a开始下载ing...");
                            try {
                                File file = Mc9yApi.installCloudPlugin(downloadLink);
                                sender.sendMessage("§a插件:"+file.getName()+"安装完成!");
                            } catch (Exception e) {
                                sender.sendMessage("§c插件安装失败!(可能没下载成功[自己去下]或者需要重启)");
                                sender.sendMessage("§c帖子地址:§3"+downloadLink);
                                e.printStackTrace();
                            }
                        }
                    }.runTaskAsynchronously(FI9yData.plugin);
                    return false;
                }
                case "latestresources":{
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            cacheSearchData(Mc9yApi.getLatestResources());
                            SomeMethod.showCache(sender);
                            sender.sendMessage("§a搜索完成");
                        }
                    }.runTaskAsynchronously(FI9yData.plugin);
                    return false;
                }
                case "reload":{
                    sender.sendMessage("§a重载中ing...");
                    FI9yData.plugin.reloadConfig();
                    sender.sendMessage("§a重载完成");
                    return false;
                }
                case "showcache":{
                    SomeMethod.showCache(sender);
                    return false;
                }
            }
        }
        sender.sendMessage("help待写");
        return false;
    }

    private static void cacheSearchData(Post[] search) {
        int i = 0;
        for (Post post : search) {
            FI9yData.searchCache.put(i,post);
            i++;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length<1) return CMD_LIST;
        if (args.length==1) return CMD_LIST.stream()
                .filter(s->s.startsWith(args[0]))
                .collect(Collectors.toList());
        return null;
    }
}
