import org.apache.log4j.BasicConfigurator;

import com.sun.jna.NativeLibrary;

import uk.co.caprica.vlcj.runtime.RuntimeUtil;

public class Main {
    public static void main(String[] args) {
        // 解决日志报错问题
        BasicConfigurator.configure();

        // 关闭日志
        // Logger.getRootLogger().shutdown();

        // 加载dll
        NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), "vlc");

        Window frame = new Window();

        // 旧版方式一：直接传文件夹
        // String videoFolder = "E:\\tmp\\test\\videoTest";
        // Window frame = new Window(videoFolder);
        // frame.setVisible(true);
        // frame.play();

        // 旧版方式二：传文件
        // String filePath = "E:\\tmp\\test\\videoTest\\01_编程语言运行机制.avi";
        // EmbeddedMediaPlayer mediaPlayer = frame.getMediaPlayer();
        // mediaPlayer.playMedia(filePath);

    }

}
