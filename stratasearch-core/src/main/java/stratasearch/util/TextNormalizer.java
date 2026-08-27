package stratasearch.util;

// Manual text helpers. No regex, no library split/join/lowercase.
public class TextNormalizer {

    public static char NormalizeChar(char C) {
        if (C >= 'A' && C <= 'Z') {
            return (char) (C + 32);
        }
        if (C < 32 || C > 126) {
            return ' ';
        }
        return C;
    }

    public static String Normalize(String Raw) {
        if (Raw == null) {
            return "";
        }
        char[] Buf = new char[Raw.length()];
        for (int I = 0; I < Raw.length(); I++) {
            Buf[I] = NormalizeChar(Raw.charAt(I));
        }
        return new String(Buf);
    }

    public static boolean IsWordChar(char C) {
        return (C >= 'a' && C <= 'z') || (C >= '0' && C <= '9');
    }

    public static ManualStringList SplitWords(String Text) {
        ManualStringList Words = new ManualStringList();
        int I = 0;
        int N = Text.length();
        while (I < N) {
            while (I < N && !IsWordChar(Text.charAt(I))) {
                I++;
            }
            int Start = I;
            while (I < N && IsWordChar(Text.charAt(I))) {
                I++;
            }
            if (I > Start) {
                Words.Add(Text.substring(Start, I));
            }
        }
        return Words;
    }

    public static int CountSentenceEnds(String Text) {
        int Ends = 0;
        for (int I = 0; I < Text.length(); I++) {
            char C = Text.charAt(I);
            if (C == '.' || C == '!' || C == '?') {
                Ends++;
            }
        }
        return Ends;
    }

    public static int CountLines(String[] Documents) {
        int Lines = 0;
        for (int D = 0; D < Documents.length; D++) {
            int DocLines = 0;
            for (int I = 0; I < Documents[D].length(); I++) {
                if (Documents[D].charAt(I) == '\n') {
                    DocLines++;
                }
            }
            if (Documents[D].length() > 0) {
                DocLines++;
            }
            Lines += DocLines;
        }
        return Lines;
    }

    // Corpus is treated as one joined text stream separated by newlines.
    // Each document is normalized first so '\n' separators survive.
    public static String JoinDocuments(String[] Documents) {
        int Total = 0;
        for (int D = 0; D < Documents.length; D++) {
            Total += Documents[D].length();
            if (D > 0) {
                Total += 1;
            }
        }
        char[] Buf = new char[Total];
        int Write = 0;
        for (int D = 0; D < Documents.length; D++) {
            if (D > 0) {
                Buf[Write] = '\n';
                Write++;
            }
            String Clean = Normalize(Documents[D]);
            for (int I = 0; I < Clean.length(); I++) {
                Buf[Write] = Clean.charAt(I);
                Write++;
            }
        }
        return new String(Buf);
    }

    public static ManualStringList SplitComma(String Line) {
        ManualStringList Parts = new ManualStringList();
        int I = 0;
        int N = Line.length();
        while (I <= N) {
            int Start = I;
            while (I < N && Line.charAt(I) != ',') {
                I++;
            }
            String Piece = Line.substring(Start, I);
            Parts.Add(Piece);
            if (I == N) {
                break;
            }
            I++;
        }
        return Parts;
    }

    public static String Trim(String S) {
        int Start = 0;
        int End = S.length();
        while (Start < End && S.charAt(Start) == ' ') {
            Start++;
        }
        while (End > Start && S.charAt(End - 1) == ' ') {
            End--;
        }
        return S.substring(Start, End);
    }
}
