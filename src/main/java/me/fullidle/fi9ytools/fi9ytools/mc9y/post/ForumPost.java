package me.fullidle.fi9ytools.fi9ytools.mc9y.post;

import lombok.Getter;
import lombok.SneakyThrows;
import me.fullidle.fi9ytools.fi9ytools.util.SomeMethod;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.util.ArrayList;

import static me.fullidle.fi9ytools.fi9ytools.data.FI9yData.client;

@Getter
public class ForumPost implements Post{
    private final MessageBlock[] messageBlock;
    private ForumPost(MessageBlock[] messageBlock){
        this.messageBlock = messageBlock;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getPublisher() {
        return null;
    }

    @Override
    public PostType getPostType() {
        return null;
    }

    @Override
    public String getPostURL() {
        return null;
    }

    @SneakyThrows
    public static ForumPost getInstance(String pluginURL){
        Request request = SomeMethod.getDefaultGETMethodBuilder(pluginURL).build();
        Response response = client.newCall(request).execute();
        Document parse = Jsoup.parse(response.body().string(), Parser.htmlParser());
        Elements block = parse.select(".message--post div.message-inner");
        ArrayList<MessageBlock> list = new ArrayList<>();
        for (Element el : block) {
            list.add(new MessageBlock(
                    el.select(".message-body div.bbWrapper").text(),
                    el.select("a[itemprop='name']").text()
            ));
        }
        return new ForumPost(list.toArray(new MessageBlock[0]));
    }

    @Getter
    public static class MessageBlock{
        private final String message;
        private final String user;

        public MessageBlock(String message,String user){
            this.message = message;
            this.user = user;
        }
    }
}
