package ar.rulosoft.mimanganu.componentes;

import android.content.Context;
import android.text.Html;

import java.io.File;
import java.util.Comparator;

import ar.rulosoft.mimanganu.FragmentMisMangas;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.services.DownloadPoolService;

public class Chapter{

    public static final int NEW = -1;
    public static final int UNREAD = 0;
    public static final int READ = 1;
    public static final int READING = 2;

    private int id;
    private int pages;
    private int mangaID;
    private int pagesRead;
    private int readStatus;
    private String title;
    private String path;
    private String extra;
    private boolean finished;
    private boolean downloaded;
    private float volatile_order = -1;

    public Chapter(String title, String path) {
        super();
        this.title = Html.fromHtml(title).toString();
        this.path = path;
    }

    public int getMangaID() {
        return mangaID;
    }

    public void setMangaID(int mangaID) {
        this.mangaID = mangaID;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    @Override
    public String toString() {
        return title;
    }

    public int getPagesRead() {
        return pagesRead;
    }

    public void setPagesRead(int pagesRead) {
        this.pagesRead = pagesRead;
    }

    public int getReadStatus() {
        return readStatus;
    }

    public void setReadStatus(int readStatus) {
        this.readStatus = readStatus;
    }

    public boolean isDownloaded() {
        return downloaded;
    }

    public void setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public void delete(Context context, Manga manga, ServerBase s) {
        deleteImages(context, manga, s);
        Database.deleteChapter(context, this);
    }

    public void delete(Context context) {
        Manga manga = Database.getManga(context, getMangaID());
        ServerBase s = ServerBase.getServer(manga.getServerId());
        delete(context, manga, s);
    }

    public void deleteImages(Context context) {
        Manga manga = Database.getManga(context, getMangaID());
        ServerBase s = ServerBase.getServer(manga.getServerId());
        deleteImages(context, manga, s);
    }

    private void deleteImages(Context context, Manga manga, ServerBase s) {
        String path = DownloadPoolService.generateBasePath(s, manga, this, context);
        FragmentMisMangas.deleteRecursive(new File(path));
    }

    public void reset(Context context, Manga manga, ServerBase s) {
        String path = DownloadPoolService.generateBasePath(s, manga, this, context);
        FragmentMisMangas.deleteRecursive(new File(path));
        setPages(0);
        setDownloaded(false);
        setPagesRead(0);
        Database.updateChapterPlusDownload(context, this);
    }

    public void freeSpace(Context context) {
        deleteImages(context);
        setDownloaded(false);
        Database.updateChapterPlusDownload(context, this);
    }

    public void freeSpace(Context context, Manga manga, ServerBase s) {
        deleteImages(context, manga, s);
        setDownloaded(false);
        Database.updateChapterPlusDownload(context, this);
    }

    public void reset(Context context) {
        Manga manga = Database.getManga(context, getMangaID());
        ServerBase s = ServerBase.getServer(manga.getServerId());
        reset(context, manga, s);
    }

    public void markRead(Context c, boolean read) {
        Database.markChapter(c, getId(), read);
        setReadStatus(read ? Chapter.READ : Chapter.UNREAD);
    }

    public void addChapterFirst(Manga manga) {
        manga.getChapters().add(0, this);
    }

    public static class Comparators{
        private static final String FLOAT_PATTERN = "([.,0123456789]+)";
        private static final String STRING_END_PATTERN = "[^\\d]\\.";
        private static final String VOLUME_REMOVE_PATTERN = "[v|V][o|O][l|L].{0,1}\\d+";
        public static Comparator<Chapter> NUMBERS_DSC = new Comparator<Chapter>() {
            @Override
            public int compare(Chapter c1, Chapter c2) {
                try {
                    if(c1.volatile_order == -1) {
                        String str1 = c1.getTitle().replaceAll(VOLUME_REMOVE_PATTERN, " ");
                        str1 = str1.replaceAll(STRING_END_PATTERN, " ");
                        str1 = ServerBase.getFirstMatch(FLOAT_PATTERN, str1, "");
                        c1.volatile_order = Float.parseFloat(str1);
                    }
                    if(c2.volatile_order == -1) {
                        String str2 = c2.getTitle().replaceAll(VOLUME_REMOVE_PATTERN, " ");
                        str2 = str2.replaceAll(STRING_END_PATTERN, " ");
                        str2 = ServerBase.getFirstMatch(FLOAT_PATTERN, str2, "");
                        c2.volatile_order = Float.parseFloat(str2);
                    }
                    return  (int)Math.floor(c2.volatile_order-c1.volatile_order);
                } catch (Exception e) {
                    return  0;
                }
            }
        };
        public static Comparator<Chapter> NUMBERS_ASC = new Comparator<Chapter>() {
            @Override
            public int compare(Chapter c1, Chapter c2) {
                try {
                    if(c1.volatile_order == -1) {
                        String str1 = c1.getTitle().replaceAll(VOLUME_REMOVE_PATTERN, " ");
                        str1 = str1.replaceAll(STRING_END_PATTERN, " ");
                        str1 = ServerBase.getFirstMatch(FLOAT_PATTERN, str1, "");
                        c1.volatile_order = Float.parseFloat(str1);
                    }
                    if(c2.volatile_order == -1) {
                        String str2 = c2.getTitle().replaceAll(VOLUME_REMOVE_PATTERN, " ");
                        str2 = str2.replaceAll(STRING_END_PATTERN, " ");
                        str2 = ServerBase.getFirstMatch(FLOAT_PATTERN, str2, "");
                        c2.volatile_order = Float.parseFloat(str2);
                    }
                    return  (int)Math.floor(c1.volatile_order-c2.volatile_order);
                } catch (Exception e) {
                    return  0;
                }
            }
        };
        public static Comparator<Chapter> TITLE_DSC = new Comparator<Chapter>() {
            @Override
            public int compare(Chapter c1, Chapter c2) {
                return c1.getTitle().compareTo(c2.getTitle());
            }
        };
        public static Comparator<Chapter> TITLE_ASC = new Comparator<Chapter>() {
            @Override
            public int compare(Chapter c1, Chapter c2) {
                return c2.getTitle().compareTo(c1.getTitle());
            }
        };
        public static Comparator<Chapter> DATABASE_ADDED = new Comparator<Chapter>() {
            @Override
            public int compare(Chapter c1, Chapter c2) {
                return (c2.getId() - c1.getId());
            }
        };
    }
}
