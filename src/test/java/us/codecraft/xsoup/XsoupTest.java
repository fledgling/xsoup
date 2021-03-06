package us.codecraft.xsoup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author code4crafter@gmail.com
 */
public class XsoupTest {

    private String html = "<html><body><div id='test'>aaa<div><a href=\"https://github.com\">github.com</a></div></div></body></html>";

    private String htmlClass = "<html><body><div class='a b c'><div><a href=\"https://github.com\">github.com</a></div></div><div>b</div></body></html>";

    @Test
    public void testSelect() {

        String html = "<html><div><a href='https://github.com'>github.com</a></div>" +
                "<table><tr><td>a</td><td>b</td></tr></table></html>";

        Document document = Jsoup.parse(html);

        String result = Xsoup.compile("//a/@href").evaluate(document).get();
        assertThat(result).isEqualTo("https://github.com");

        List<String> list = Xsoup.compile("//tr/td/text()").evaluate(document).list();
        assertThat(list).contains("a","b");
    }

    @Test
    public void testParent() {

        Document document = Jsoup.parse(html);

        String result = Xsoup.select(document, "/html/body/div/div/a").get();
        assertThat(result).isEqualTo("<a href=\"https://github.com\">github.com</a>");

        result = Xsoup.select(document, "/html//div/div/a").get();
        assertThat(result).isEqualTo("<a href=\"https://github.com\">github.com</a>");

        result = Xsoup.select(document, "/html/div/div/a").get();
        assertThat(result).isNull();

    }

    @Test
    public void testByAttribute() {

        Document document = Jsoup.parse(html);

        String result = Xsoup.select(document, "//a[@href]").get();
        assertThat(result).isEqualTo("<a href=\"https://github.com\">github.com</a>");

        result = Xsoup.select(document, "//a[@id]").get();
        assertThat(result).isNull();

        result = Xsoup.select(document, "//div[@id=test]").get();
        String expectedDiv = "<div id=\"test\">\n" +
                " aaa\n" +
                " <div>\n" +
                "  <a href=\"https://github.com\">github.com</a>\n" +
                " </div>\n" +
                "</div>";
        assertThat(result).isEqualTo(expectedDiv);

        result = Xsoup.select(document, "//div[@id='test']").get();
        assertThat(result).isEqualTo(expectedDiv);
        result = Xsoup.select(document, "//div[@id=\"test\"]").get();
        assertThat(result).isEqualTo(expectedDiv);
    }

    @Test
    public void testClass() {

        Document document = Jsoup.parse(htmlClass);

        String result = Xsoup.select(document, "//div[@class=a]").get();
        assertThat(result).isEqualTo("<div class=\"a b c\">\n" +
                " <div>\n" +
                "  <a href=\"https://github.com\">github.com</a>\n" +
                " </div>\n" +
                "</div>");

        result = Xsoup.select(document, "//div[@class=d]").get();
        assertThat(result).isNull();

    }

    @Test
    public void testNth() {

        Document document = Jsoup.parse(htmlClass);

        String result = Xsoup.select(document, "//body/div[1]").get();
        assertThat(result).isEqualTo("<div class=\"a b c\">\n" +
                " <div>\n" +
                "  <a href=\"https://github.com\">github.com</a>\n" +
                " </div>\n" +
                "</div>");

        result = Xsoup.select(document, "//body/div[2]").get();
        assertThat(result).isEqualTo("<div>\n" +
                " b\n" +
                "</div>");

    }

    @Test
    public void testAttribute() {

        Document document = Jsoup.parse(htmlClass);

        String result = Xsoup.select(document, "//a/@href").get();
        assertThat(result).isEqualTo("https://github.com");

        result = Xsoup.select(document, "//a/text()").get();
        assertThat(result).isEqualTo("github.com");

        result = Xsoup.select(document, "//div[@class=a]/html()").get();
        assertThat(result).isEqualTo("<div>\n" +
                " <a href=\"https://github.com\">github.com</a>\n" +
                "</div>");

    }

    @Test
    public void testWildcard() {

        Document document = Jsoup.parse(htmlClass);

        String result = Xsoup.select(document, "//*[@href]/@href").get();
        assertThat(result).isEqualTo("https://github.com");

        result = Xsoup.select(document, "//*[@class=a]/html()").get();
        assertThat(result).isEqualTo("<div>\n" +
                " <a href=\"https://github.com\">github.com</a>\n" +
                "</div>");

        List<String> list = Xsoup.select(document, "//*[@*]/html()").list();
        assertThat(list.get(0)).isEqualTo("<div>\n" +
                " <a href=\"https://github.com\">github.com</a>\n" +
                "</div>");
        assertThat(list.get(1)).isEqualTo("github.com");
    }

    @Test
    public void testFuzzyValueMatch() {

        Document document = Jsoup.parse(html);

        String result = Xsoup.select(document, "//*[@id~=te]/text()").get();
        assertThat(result).isEqualTo("aaa");
        result = Xsoup.select(document, "//*[@id$=st]/text()").get();
        assertThat(result).isEqualTo("aaa");
        result = Xsoup.select(document, "//*[@id*=es]/text()").get();
        assertThat(result).isEqualTo("aaa");
        result = Xsoup.select(document, "//*[@id~='tes[t]+']/text()").get();
        assertThat(result).isEqualTo("aaa");

        result = Xsoup.select(document, "//*[@id~=te]/allText()").get();
        assertThat(result).isEqualTo("aaa github.com");
    }

    @Test
    public void testLogicOperation() {

        Document document = Jsoup.parse(html);

        String result = Xsoup.select(document, "//*[@id=te or @id=test]/text()").get();
        assertThat(result).isEqualTo("aaa");

        result = Xsoup.select(document, "//*[@id=test or @id=te]/text()").get();
        assertThat(result).isEqualTo("aaa");

        result = Xsoup.select(document, "//*[@id=te and @id=test]/text()").get();
        assertThat(result).isNull();

        result = Xsoup.select(document, "//*[(@id=te or @id=test) and @id=test]/text()").get();
        assertThat(result).isEqualTo("aaa");

        result = Xsoup.select(document, "//*[@id=te or (@id=test and @id=id)]/text()").get();
        assertThat(result).isNull();
    }

    @Test
    public void testRegex() {

        Document document = Jsoup.parse(html);

        String result = Xsoup.select(document, "//*[@id~=te]/regex('gi\\w+ub')").get();
        assertThat(result).isEqualTo("github");

        result = Xsoup.select(document, "//a/regex('@href','.*gi\\w+ub.*')").get();
        assertThat(result).isEqualTo("https://github.com");

        result = Xsoup.select(document, "//a/regex('@href','.*(gi\\w+ub).*',1").get();
        assertThat(result).isEqualTo("github");
    }

    @Test
    public void testContains() {

        Document document = Jsoup.parse(html);

        String result = Xsoup.select(document, "//div[contains(@id,'te')]").get();
        assertThat(result).isEqualTo("<div id=\"test\">\n" +
                " aaa\n" +
                " <div>\n" +
                "  <a href=\"https://github.com\">github.com</a>\n" +
                " </div>\n" +
                "</div>");

    }

    @Test
    public void testCombingXPath() {

        String html2 = "<html><div id='test2'>aa<a href='https://github.com'>github.com</a></div>";

        Document document = Jsoup.parse(html);

        String result = Xsoup.select(document, "//div[@id='test']/text() | //div[@id='test2']/text()").get();
        assertThat(result).isEqualTo("aaa");

        result = Xsoup.select(html2, "//div[@id='test']/text() | //div[@id='test2']/text()").get();
        assertThat(result).isEqualTo("aa");

        List<String> resultList = Xsoup.select(html + html2, "//div[@id='test']/text() | //div[@id='test2']/text()").list();
        assertThat(resultList).contains("aaa", "aa");
    }

}
