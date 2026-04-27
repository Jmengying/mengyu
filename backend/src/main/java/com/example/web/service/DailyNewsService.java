package com.example.web.service;

import com.example.web.entity.Article;
import com.example.web.entity.User;
import com.example.web.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import javax.net.ssl.*;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class DailyNewsService {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private UserMapper userMapper;

    private Long botUserId;
    private int[] publishHours = {8, 14, 20};
    private boolean[] publishedToday = new boolean[3];
    private boolean caughtUpToday = false;
    private final RestTemplate restTemplate = createUnsafeRestTemplate();

    private final List<String[]> fallbackNews = List.of(
        new String[]{"今日动漫趣闻：经典作品重映热潮", "<h2>今日动漫趣闻</h2><p>据多家媒体报道，多部经典动漫作品宣布将推出重映计划。"},
        new String[]{"春季番剧最新情报——多部作品收视率创新高", "<h2>春季番剧情报</h2><p>2026年春季番剧播出已有一段时间，多部作品收视率和口碑都表现出色。"},
        new String[]{"声优资讯：人气声优加盟新作引发期待", "<h2>声优资讯</h2><p>近日多部动画作品公布了追加声优阵容。"},
        new String[]{"动漫音乐资讯：人气主题曲配信突破百万", "<h2>动漫音乐资讯</h2><p>本季多部动画的主题曲取得了优异成绩。"},
        new String[]{"动画电影票房速报——新片表现强势", "<h2>动画电影票房速报</h2><p>本周末多部动画电影票房成绩喜人。"},
        new String[]{"二次元周边资讯：限定手办与联名企划", "<h2>周边资讯</h2><p>各大厂商近期公布了多款全新动漫周边产品。"}
    );

    private static RestTemplate createUnsafeRestTemplate() {
        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, new TrustManager[]{new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] c, String a) {}
                public void checkServerTrusted(X509Certificate[] c, String a) {}
                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
            }}, null);
            HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((h, s) -> true);
        } catch (Exception ignored) {}
        return new RestTemplate();
    }

    @PostConstruct
    public void init() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, "moe-bot");
        User bot = userMapper.selectOne(wrapper);
        if (bot != null) botUserId = bot.getId();
        scheduleCatchUp();
    }

    private void scheduleCatchUp() {
        if (caughtUpToday) return;
        caughtUpToday = true;
        int now = LocalTime.now().getHour();
        int latestMissed = -1;
        for (int i = 0; i < publishHours.length; i++) {
            if (now >= publishHours[i] && !publishedToday[i]) {
                latestMissed = i;
            }
        }
        if (latestMissed >= 0) {
            final int idx = latestMissed;
            new Thread(() -> {
                try { Thread.sleep(15000); } catch (Exception e) {}
                publishByIndex(idx);
            }).start();
        }
    }

    public int[] getPublishHours() { return publishHours.clone(); }

    public void setPublishHours(int[] hours) {
        if (hours != null && hours.length == 3) {
            for (int h : hours) { if (h < 0 || h > 23) return; }
            this.publishHours = hours;
            this.publishedToday = new boolean[3];
            scheduleCatchUp();
        }
    }

    public void publishNow() { publishByIndex(0); }

    @Scheduled(fixedDelay = 300000)
    public void checkAndPublish() {
        int now = LocalTime.now().getHour();
        for (int i = 0; i < publishHours.length; i++) {
            if (!publishedToday[i] && now == publishHours[i]) {
                publishByIndex(i);
            }
        }
    }

    private void publishByIndex(int idx) {
        if (botUserId == null) return;
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy年M月d日"));

        List<Map<String, String>> rssItems = fetchRssNews();
        String title, content, summary, coverUrl = "";

        if (!rssItems.isEmpty()) {
            int rssIdx = (LocalDate.now().getDayOfYear() * 3 + idx) % rssItems.size();
            Map<String, String> item = rssItems.get(rssIdx);
            String rawTitle = item.get("title");
            String rawDesc = item.get("description");
            String rawFull = item.get("fullContent");

            title = "📰 " + today + " " + rawTitle;
            summary = rawTitle;

            // Remove RSS artifacts like "A[" at start
            if (rawDesc != null) rawDesc = rawDesc.replaceAll("^\\s*A\\[", "");
            if (rawFull != null && !rawFull.isEmpty()) {
                rawFull = rawFull.replaceAll("^\\s*A\\[", "");
                java.util.regex.Matcher imgM = java.util.regex.Pattern.compile("<img[^>]+src=['\"]([^'\"]+)['\"]").matcher(rawFull);
                if (imgM.find()) {
                    coverUrl = imgM.group(1);
                    rawFull = rawFull.replaceFirst("<img[^>]+>", "");
                }
                content = "<h2>" + rawTitle + "</h2>" + rawFull;
                if (!coverUrl.isEmpty()) {
                    content = "<img src='" + coverUrl + "' style='width:100%;border-radius:8px;margin:0 0 16px 0;max-height:400px;object-fit:cover' alt='封面'/>" + content;
                }
            } else {
                content = "<h2>" + rawTitle + "</h2><p>" + rawDesc + "</p>";
            }
        } else {
            int fi = (LocalDate.now().getDayOfYear() * 3 + idx) % fallbackNews.size();
            String[] news = fallbackNews.get(fi);
            title = "📰 " + today + " " + news[0];
            summary = news[0];
            content = news[1];
        }

        Article article = new Article();
        article.setUserId(botUserId);
        article.setTitle(title);
        article.setContent(content);
        article.setCategory("general");
        article.setSummary(summary);
        if (!coverUrl.isEmpty()) article.setCoverImage(coverUrl);
        try {
            articleService.createArticle(article);
            publishedToday[idx] = true;
            System.out.println("✅ 每日新闻 #" + (idx+1) + " 已发布: " + title);
        } catch (Exception e) {
            System.err.println("❌ 每日新闻 #" + (idx+1) + " 发布失败: " + e.getMessage());
        }
    }

    private List<Map<String, String>> fetchRssNews() {
        String[] sources = {"https://www.hotacg.com/feed/", "https://acg17.com/feed/", "https://myanimelist.net/rss/news.xml"};

        for (String url : sources) {
            try {
                String xml = restTemplate.getForObject(url, String.class);
                if (xml == null) continue;
                List<Map<String, String>> items = new ArrayList<>();
                String[] entries = xml.split("<item>");
                for (int i = 1; i < entries.length && items.size() < 15; i++) {
                    String entry = entries[i].split("</item>")[0];
                    String t = extractXmlTag(entry, "title");
                    String d = extractXmlTag(entry, "description");
                    String img = extractXmlTag(entry, "media:thumbnail");
                    if (t != null) {
                        String titleText = unescapeXml(t);
                        String lowerTitle = titleText.toLowerCase();
                        String[] excludeWords = {"漫展", "攻略", "萤火虫", "游戏", "手办", "周边", "活动"};
                        boolean skip = false;
                        for (String w : excludeWords) { if (lowerTitle.contains(w)) { skip = true; break; } }
                        if (skip) continue;

                        String cleanDesc = d != null ? d.replaceAll("<!\\[CDATA\\[|\\]\\]>", "").trim() : "";
                        cleanDesc = cleanDesc.replaceAll("<[^>]+>", "").trim();
                        cleanDesc = cleanDesc.replaceAll("^\\s*A\\[", "");
                        String fullContent = extractFullContent(entry);
                        if (fullContent != null) {
                            fullContent = fullContent.replaceAll("^\\s*A\\[", "");
                            fullContent = cleanHtmlContent(fullContent);
                        }

                        Map<String, String> map = new HashMap<>();
                        map.put("title", titleText);
                        map.put("description", unescapeXml(cleanDesc));
                        map.put("image", img != null ? img.trim() : "");
                        map.put("fullContent", fullContent != null ? fullContent : "");
                        items.add(map);
                    }
                }
                if (!items.isEmpty()) {
                    System.out.println("✅ RSS获取成功: " + url + " (" + items.size() + "条)");
                    return items;
                }
            } catch (Exception e) {
                System.out.println("⚠️ RSS获取失败: " + url + " - " + e.getMessage());
            }
        }
        return List.of();
    }

    private String extractXmlTag(String xml, String tag) {
        int start = xml.indexOf("<" + tag + ">");
        if (start == -1) {
            start = xml.indexOf("<" + tag + " ");
            if (start >= 0) { start = xml.indexOf(">", start) + 1; }
        } else { start += tag.length() + 2; }
        if (start < 2) return null;
        int end = xml.indexOf("</" + tag + ">", start);
        if (end == -1) return null;
        return xml.substring(start, end).trim();
    }

    private String extractFullContent(String xml) {
        int cs = xml.indexOf("<content:encoded><![CDATA[");
        if (cs >= 0) {
            int ce = xml.indexOf("]]></content:encoded>", cs);
            if (ce > cs) return xml.substring(cs + 24, ce);
        }
        cs = xml.indexOf("<content:encoded>");
        if (cs >= 0) {
            int ce = xml.indexOf("</content:encoded>", cs);
            if (ce > cs) return xml.substring(cs + 17, ce);
        }
        return null;
    }

    private String cleanHtmlContent(String html) {
        if (html == null) return html;
        html = html.replaceAll("<iframe[^>]*>.*?</iframe>", "");
        html = html.replaceAll("<img[^>]*wp-smiley[^>]*>", "");
        html = html.replaceAll("<script[^>]*>.*?</script>", "");
        html = html.replaceAll("<style[^>]*>.*?</style>", "");
        html = html.replaceAll("<br\\s*/?>", "\n");
        html = html.replaceAll("\n{3,}", "\n\n");
        html = html.replaceAll("<p[^>]*>(&nbsp;|\\s)*</p>", "");
        html = html.replaceAll("&#8212;", "—");
        html = html.replaceAll("&#8230;", "…");
        return html.trim();
    }

    private String unescapeXml(String s) {
        return s.replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">")
                .replace("&quot;", "\"").replace("&#039;", "'").replace("&apos;", "'")
                .replaceAll("&#x27;", "'").replaceAll("&#\\d+;", "");
    }
}
