package me.fullidle.fi9ytools.fi9ytools.mc9y;

import me.fullidle.fi9ytools.fi9ytools.mc9y.post.resource.ResourcePost;
import lombok.Getter;
import lombok.SneakyThrows;
import me.fullidle.fi9ytools.fi9ytools.data.FI9yData;
import me.fullidle.fi9ytools.fi9ytools.util.SomeMethod;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

@Getter
public class User {
    private final String account;
    private final String userId;
    private final int userLevel;
    private final String userTitle;
    private final String registrationTime;
    private final ResourcePost[] resourcePost;
    private User(String account, String userId, int userLevel, String userTitle, String registrationTime, ResourcePost[] resourcePost){
        this.account = account;
        this.userId = userId;
        this.userLevel = userLevel;
        this.userTitle = userTitle;
        this.registrationTime = registrationTime;
        this.resourcePost = resourcePost;
    }

    @SneakyThrows
    public static User getInstance(String account){
        String url = null;
        Request request = SomeMethod.getDefaultGETMethodBuilder(url).build();
        Response response = FI9yData.client.newCall(request).execute();
        Document parse = Jsoup.parse(response.body().string(), Parser.htmlParser());
        /*待写*/

        response.close();
        return null;
    }
}
