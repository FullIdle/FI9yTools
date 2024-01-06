package me.fullidle.fi9ytools.fi9ytools.mc9y.post;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.SneakyThrows;
import me.fullidle.fi9ytools.fi9ytools.util.SomeMethod;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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
    private final String downloadLink;
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
        String jsonString = parse.selectFirst("script[type=application/ld+json]").data();
        JsonObject json = gson.fromJson(jsonString, JsonObject.class);
        /*获取元素数据*/
        String downloadLink = request.url().resolve(parse.selectFirst("a.button--cta").attr("href")).toString();
        Elements select = parse.select(".pairs--justified dd");
        String price = select.get(0).text();
        String numberOfViews = select.get(2).text();
        String numberOfDownloads = select.get(1).text();
        String evaluate = select.get(5).text();
        String tag = parse.select(".p-title-value span.label").first().text();
        response.close();
        /*获取json数据*/
        String title = json.get("name").getAsString();
        String publisher = json.get("author").getAsJsonObject().get("name").getAsString();
        String alternativeHeadline = json.get("alternativeHeadline").getAsString();
        String content = json.get("description").getAsString();
        String firstPublished = json.get("dateCreated").getAsString();
        String lastUpdated = json.get("dateModified").getAsString();
        String currentVersion = json.get("version").getAsString();
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
