package me.fullidle.fi9ytools.fi9ytools.mc9y.post.resource;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.SneakyThrows;
import me.fullidle.fi9ytools.fi9ytools.mc9y.post.Post;
import me.fullidle.fi9ytools.fi9ytools.util.SomeMethod;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import static me.fullidle.fi9ytools.fi9ytools.data.FI9yData.client;
import static me.fullidle.fi9ytools.fi9ytools.data.FI9yData.gson;

@Getter
public class ResourcePost implements Post {
    private final String pluginURL;
    private final String title;
    private final String publisher;
    private final String content;
    private final String firstPublished;
    private final String lastUpdated;
    private final String alternativeHeadline;
    private final String currentVersion;
    @Nullable private final String downloadLink;
    private final String numberOfViews;
    private final String numberOfDownloads;
    private final String price;
    private final String evaluate;
    private final String tag;
    private ResourcePost(String pluginURL,String title, String publisher, String content, String firstPublished, String lastUpdated, String alternativeHeadline, String currentVersion, String downloadLink, String numberOfViews, String numberOfDownloads, String price, String evaluate,String tag) {
        this.pluginURL = pluginURL;
        this.title = title;
        this.publisher = publisher;
        this.content = content;
        this.firstPublished = firstPublished;
        this.lastUpdated = lastUpdated;
        this.alternativeHeadline = alternativeHeadline;
        this.currentVersion = currentVersion;
        this.downloadLink = downloadLink;
        this.numberOfViews = numberOfViews;
        this.numberOfDownloads = numberOfDownloads;
        this.price = price;
        this.evaluate = evaluate;
        this.tag = tag;
    }

    @SneakyThrows
    public static ResourcePost getInstance(String pluginURL){
        Request request = SomeMethod.getDefaultGETMethodBuilder(pluginURL).build();
        Response response = client.newCall(request).execute();
        Document parse = Jsoup.parse(response.body().string(), Parser.htmlParser());
        String jsonString = parse.selectFirst("body[data-template=\"xfrm_resource_view\"] script[type=\"application/ld+json\"]").data();
        JsonObject json = gson.fromJson(jsonString, JsonObject.class);
        /*获取元素数据*/
        Elements select = parse.select(".pairs--justified dt");
        String price = null;
        String numberOfViews = null;
        String numberOfDownloads = null;
        String evaluate = null;
        for (Element el : select) {
            String value = el.parent().getAllElements().get(2).text();
            switch (el.text()) {
                case "价格":{
                    price = value;
                    continue;
                }
                case "下载":{
                    numberOfDownloads = value;
                    continue;
                }
                case "查看":{
                    numberOfViews = value;
                    continue;
                }
                case "评分":{
                    evaluate = value;
                }
            }
        }
        String tag = parse.selectFirst(".p-title-value span.label").text();
        Element downloadLinkE = parse.selectFirst("a.button--cta");
        String downloadLink = downloadLinkE == null?null
                :downloadLinkE.attr("href");
        response.close();
        /*获取json数据*/
        String title = json.get("name").getAsString();
        String publisher = json.get("author").getAsJsonObject().get("name").getAsString();
        String alternativeHeadline = json.get("alternativeHeadline").getAsString();
        String content = json.get("description").getAsString();
        String firstPublished = json.get("dateCreated").getAsString();
        String lastUpdated = json.get("dateModified").getAsString();
        JsonElement version = json.get("version");
        String currentVersion = version==null?null:version.getAsString();
        return new ResourcePost(pluginURL,
                title,
                publisher,
                content,
                firstPublished,
                lastUpdated,
                alternativeHeadline,
                currentVersion,
                downloadLink,
                numberOfViews,
                numberOfDownloads,
                price,
                evaluate,
                tag
        );
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getPublisher() {
        return publisher;
    }

    @Override
    public PostType getPostType() {
        return PostType.RESOURCE;
    }

    @Override
    public String getPostURL() {
        return getPluginURL();
    }

    public boolean isBukkitPlugin(){
        return tag.contains("Bukkit");
    }
}
