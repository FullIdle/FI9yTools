package me.fullidle.fi9ytools.fi9ytools.data;

import me.fullidle.fi9ytools.fi9ytools.mc9y.post.Post;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SearchCache {
    public static Map<Integer, Post> searchCache = new HashMap<>();
    public static String searchNextPageUrl;

    public static void clear(){
        searchNextPageUrl = null;
        searchCache.clear();
    }

    public static void showCache(CommandSender sender){
        for (Map.Entry<Integer, Post> entry : searchCache.entrySet()) {
            sender.sendMessage("§a"+ entry.getKey() +"."+entry.getValue().getTitle());
        }
    }

    public static void cacheSearchData(ArrayList<Post> search) {
        cacheSearchData(search.toArray(new Post[0]));
    }
    public static void cacheSearchData(Post[] search) {
        int i = 0;
        if (!searchCache.keySet().isEmpty()){
            i = Collections.max(searchCache.keySet())+1;
        }
        for (Post post : search) {
            searchCache.put(i,post);
            i++;
        }
    }
}
