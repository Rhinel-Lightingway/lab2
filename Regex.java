import java.util.*;
import java.util.regex.*;

public class Regex {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入文字（输入空行结束）：");
        StringBuilder textBuilder = new StringBuilder();
        
        // 循环读取多行输入
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.isEmpty()) { // 输入空行时结束输入
                break;
            }
            textBuilder.append(line).append("\n");
        }
        scanner.close();

        String text = textBuilder.toString();
        String html = convertTextToHtml(text);
        System.out.println("转换后的HTML输出：");
        System.out.println(html);
    }

    public static String convertTextToHtml(String text) {
        String phoneRegex = "(\\b1[3-9]\\d{9}\\b)|(0\\d{2,3}-?\\d{7,8})";
        String emailRegex = "[\\w.%+-]+@[\\w.-]+\\.[A-Za-z]{2,6}";
        String urlRegex = "((https?|ftp)://[\\w.-]+(/[\\w./?%&=]*)?)|(www\\.[\\w.-]+(/[\\w./?%&=]*)?)|([\\w.-]+\\.(com|cn|net|org|gov|edu)(/[\\w./?%&=]*)?)";
        String ipRegex = "\\b(\\d{1,3}\\.){3}\\d{1,3}\\b";

        Map<String, String> patternMap = new LinkedHashMap<>();
        patternMap.put("URL", urlRegex);
        patternMap.put("EMAIL", emailRegex);
        patternMap.put("PHONE", phoneRegex);
        patternMap.put("IP", ipRegex);

        List<Match> matches = new ArrayList<>();

        for (Map.Entry<String, String> entry : patternMap.entrySet()) {
            Pattern pattern = Pattern.compile(entry.getValue());
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                boolean exists = false;
                for (Match m : matches) {
                    if (m.start == matcher.start() && m.end == matcher.end()) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    matches.add(new Match(matcher.start(), matcher.end(), matcher.group(), entry.getKey()));
                }
            }
        }

        Collections.sort(matches, Comparator.comparingInt(m -> m.start));

        List<Match> nonOverlappingMatches = removeOverlaps(matches);

        StringBuilder sb = new StringBuilder();
        int lastIndex = 0;
        for (Match match : nonOverlappingMatches) {
            sb.append(text.substring(lastIndex, match.start));
            String replacement = generateReplacement(match);
            sb.append(replacement);
            lastIndex = match.end;
        }
        sb.append(text.substring(lastIndex));
        return sb.toString();
    }

    static class Match {
        int start;
        int end;
        String text;
        String type;

        Match(int start, int end, String text, String type) {
            this.start = start;
            this.end = end;
            this.text = text;
            this.type = type;
        }
    }

    static List<Match> removeOverlaps(List<Match> matches) {
        List<Match> result = new ArrayList<>();
        int prevEnd = -1;
        for (Match match : matches) {
            if (match.start >= prevEnd) {
                result.add(match);
                prevEnd = match.end;
            }
        }
        return result;
    }

    static String generateReplacement(Match match) {
        String href = "";
        if (match.type.equals("URL")) {
            if (match.text.matches("^(https?|ftp)://.*")) {
                href = match.text;
            } else {
                href = "http://" + match.text;
            }
        } else if (match.type.equals("EMAIL")) {
            href = "mailto:" + match.text;
        } else if (match.type.equals("PHONE")) {
            href = "tel:" + match.text;
        } else if (match.type.equals("IP")) {
            href = "http://" + match.text;
        }
        return "<a href=\"" + href + "\">" + match.text + "</a>";
    }
}
