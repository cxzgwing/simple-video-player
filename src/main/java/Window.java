import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import utils.FileUtils;

public class Window extends JFrame {
    // private final Logger logger = LoggerFactory.getLogger(Window.class);

    private static final int PROGRESS_HEIGHT = 10;
    private static final int PROGRESS_MIN_VALUE = 0;
    private static final int PROGRESS_MAX_VALUE = 100;
    private static final int WINDOW_X = 100;
    private static final int WINDOW_Y = 100;
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;
    private static final int LIST_WINDOW_WIDTH = 200;
    // 总时间
    private static String TOTAL_TIME;
    // 播放速度
    private float speed;
    // 首次播放
    private boolean firstPlay = true;

    // 播放器组件
    private EmbeddedMediaPlayerComponent mediaPlayerComponent;
    // 进度条
    private JProgressBar progress;
    // 暂停按钮
    private Button pauseButton;
    // 显示播放速度的标签
    private Label displaySpeed;
    // 显示时间
    private Label displayTime;
    // 进度定时器
    private Timer progressTimer;
    // 继续播放定时器
    private Timer continueTimer;
    // 所有视频路径
    private java.util.List<String> videos;
    // 当前播放视频的位置
    private int videoIndex;
    // 声音控制进度条
    private JProgressBar volumeProgress;
    // 音量显示标签
    private Label volumeLabel;
    // 文件对话框
    private FileDialog fileDialog;
    // 播放文件列表按钮
    private Button listButton;
    // 播放文件列表窗口
    private JFrame listWindow;
    // 播放文件列表显示内容
    private JTextArea listContent;

    public Window() {
        this.videos = new ArrayList<>(10);
        // initVideoFilesPath(videoFolder);
        // 设置默认速度为原速
        speed = 1.0f;
        // 设置窗口标题
        setTitle("VideoPlayer");
        // 设置窗口焦点监听事件：窗口打开时、窗口获得焦点时设置默认焦点为暂停按钮
        this.addWindowFocusListener(getWindowFocusListener());

        // 窗口关闭事件：释放资源并退出程序
        addWindowListener(closeWindowReleaseMedia());
        // 设置默认窗口关闭事件
        // setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 设置窗口位置
        setBounds(WINDOW_X, WINDOW_Y, WINDOW_WIDTH, WINDOW_HEIGHT);
        // 最大化显示窗口
        // setExtendedState(JFrame.MAXIMIZED_BOTH);

        // 主面板
        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        // ======播放面板======
        JPanel player = new JPanel();
        contentPane.add(player, BorderLayout.CENTER);
        contentPane.add(player);
        player.setLayout(new BorderLayout(0, 0));
        // 创建播放器组件并添加到容器中去
        mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
        player.add(mediaPlayerComponent);
        // 视频表面焦点监听：表面获得焦点时设置默认焦点为暂停按钮
        getVideoSurface().addFocusListener(videoSurfaceFocusAction());
        // getMediaPlayer().setRepeat(true); // 重复播放

        // ======底部面板======
        JPanel bottomPanel = new JPanel();
        BoxLayout boxLayout = new BoxLayout(bottomPanel, BoxLayout.Y_AXIS);
        bottomPanel.setLayout(boxLayout);
        contentPane.add(bottomPanel, BorderLayout.SOUTH);

        // ------进度条组件面板------
        JPanel progressPanel = new JPanel();
        progress = new JProgressBar();
        progress.setMinimum(PROGRESS_MIN_VALUE);
        progress.setMaximum(PROGRESS_MAX_VALUE);
        progress.setPreferredSize(getNewDimension());
        // 设置进度条中间显示进度百分比
        progress.setStringPainted(false);
        // 进度条进度的颜色
        progress.setForeground(new Color(46, 145, 228));
        // 进度条背景的颜色
        progress.setBackground(new Color(220, 220, 220));

        // 点击进度条调整视频播放指针
        progress.addMouseListener(setVideoPlayPoint());
        // 定时器
        progressTimer = getProgressTimer();
        // progressTimer.start();

        progressPanel.add(progress);
        progressPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        bottomPanel.add(progressPanel);
        // contentPane.add(progressPanel, BorderLayout.SOUTH);

        // ------按钮组件面板------
        JPanel buttonPanel = new JPanel();
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        bottomPanel.add(buttonPanel);

        displayTime = new Label();
        displayTime.setText(getTimeString());
        buttonPanel.add(displayTime);

        Button chooseButton = new Button("choose");
        fileDialog = new FileDialog(this);
        fileDialog.setMultipleMode(true);
        chooseButton.setFocusable(false);
        chooseButton.addMouseListener(mouseClickedChooseFiles());
        buttonPanel.add(chooseButton);

        // 重置按钮：设置播放速度为原速
        Button resetButton = new Button("reset");
        resetButton.setFocusable(false);
        resetButton.addMouseListener(mouseClickedResetSpeed());
        buttonPanel.add(resetButton);

        // 暂停/播放按钮
        pauseButton = new Button("play");
        pauseButton.setPreferredSize(new Dimension(49, 23));
        pauseButton.addKeyListener(spaceKeyPressMediaPause());
        pauseButton.addMouseListener(mouseClickedMediaPause());
        buttonPanel.add(pauseButton);

        // 倍速播放按钮：每次递增0.5，最大为3倍速
        Button fastForwardButton = new Button(">>>");
        fastForwardButton.setFocusable(false);
        fastForwardButton.addMouseListener(mouseClickedFastForward());
        buttonPanel.add(fastForwardButton);

        // 播放速度显示按钮
        displaySpeed = new Label();
        displaySpeed.setText("x" + speed);
        displaySpeed.setFocusable(false);
        displaySpeed.setEnabled(false);
        buttonPanel.add(displaySpeed);

        // 添加声音控制进度条
        volumeProgress = new JProgressBar();
        volumeProgress.setFocusable(false);
        volumeProgress.setMinimum(0);
        volumeProgress.setMaximum(100);
        volumeProgress.setValue(100);
        volumeProgress.setPreferredSize(new Dimension(100, 10));
        volumeProgress.addMouseListener(mouseClickedSetVolumeValue());
        buttonPanel.add(volumeProgress);

        // 音量显示
        volumeLabel = new Label();
        volumeLabel.setFocusable(false);
        volumeLabel.setEnabled(false);
        setVolumeLabel(volumeProgress.getValue());
        buttonPanel.add(volumeLabel);

        // 播放文件列表显示内容
        listContent = new JTextArea();
        listContent.setLineWrap(true);
        listContent.setFocusable(false);

        // 播放文件列表按钮
        listButton = new Button("list");
        listButton.setFocusable(false);
        listButton.addMouseListener(mouseClickedSetListWindow());
        buttonPanel.add(listButton);

        // 监听窗口大小，设置进度条宽度为窗口宽度（但是对于最大化和还原窗口无效，原因未知<-_->）
        this.addComponentListener(windowResizedResetProgressWidth());
        // 监听窗口最大化和还原，设置进度条宽度为窗口宽度
        this.addWindowStateListener(windowStateChangedResetProgressWidth());
        // 监听鼠标滑轮滚动，设置音量
        this.addMouseWheelListener(mouseWheelMovedSetVolume());
        this.addComponentListener(windowMovedAction());

        continueTimer = getContinueTimer();
        // continueTimer.start();

        // 设置窗口最小值
        this.setMinimumSize(new Dimension(600, 400));

        // 设置窗口可见
        this.setVisible(true);
    }

    private ComponentAdapter windowMovedAction() {
        return new ComponentAdapter() {
            @Override
            public void componentMoved(ComponentEvent e) {
                setListWindowInvisible();
            }
        };
    }

    private MouseAdapter mouseClickedSetListWindow() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (listWindow == null) {
                    // 播放文件列表窗口
                    listWindow = new JFrame();
                    listWindow.add(listContent);
                    listWindow.setUndecorated(true);
                    // 设置透明度
                    listWindow.setOpacity(0.8f);
                    setListWindowBounds();
                    listWindow.setVisible(true);
                    setListWindowShownColor();
                    listWindow.addComponentListener(setListWindowBackgroundWhenShownOrHidden());
                    return;
                }
                int x = getX();
                int width = getWidth();
                if (WINDOW_X != x || WINDOW_WIDTH != width) {
                    setListWindowBounds();
                }
                boolean visible = listWindow.isVisible();
                if (visible) {
                    listWindow.setVisible(false);
                } else {
                    listWindow.setVisible(true);
                }
            }
        };
    }

    private ComponentAdapter setListWindowBackgroundWhenShownOrHidden() {
        return new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                setListWindowShownColor();
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                setListWindowHiddenColor();
            }
        };
    }

    private void setListWindowHiddenColor() {
        listButton.setBackground(new Color(238, 238, 238));
    }

    private void setListWindowShownColor() {
        listButton.setBackground(new Color(141, 141, 141));
    }

    private void setListWindowBounds() {
        if (listWindow != null) {
            listWindow.setBounds(getWidth() + getX() - LIST_WINDOW_WIDTH - 6, getY() + 37,
                    LIST_WINDOW_WIDTH - 8, getHeight() - 100);
        }
    }

    private MouseAdapter mouseClickedChooseFiles() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                fileDialog.setVisible(true);
                File[] files = fileDialog.getFiles();
                videos.clear();
                listContent.setText("");
                for (File file : files) {
                    videos.add(file.getAbsolutePath());
                    listContent.append(videos.size() + "." + file.getName() + "\n");
                }
                videos.sort(Comparator.naturalOrder());
                if (!Objects.isNull(getMediaPlayer())) {
                    getMediaPlayer().stop();
                }
                pauseButton.setLabel("play");
                firstPlay = true;
                setProgress(0, 0);
                progressTimer.stop();
                continueTimer.stop();
                videoIndex = 0;
                loading();
            }
        };
    }

    private MouseAdapter mouseWheelMovedSetVolume() {
        return new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                // 1-下，-1-上
                int wheelRotation = e.getWheelRotation();
                if (wheelRotation == 1) {
                    // 减小音量
                    setVolume(volumeProgress.getValue() - 5);
                } else if (wheelRotation == -1) {
                    // 增大音量
                    setVolume(volumeProgress.getValue() + 5);
                }
            }
        };
    }

    private void setVolumeLabel(int value) {
        volumeLabel.setText(value + "%");
    }

    private MouseAdapter mouseClickedSetVolumeValue() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setVolume(e.getX());
            }
        };
    }

    private void setVolume(int value) {
        if (value < 0) {
            value = 0;
        } else if (value > 100) {
            value = 100;
        }
        if (volumeProgress.getValue() == value) {
            return;
        }
        volumeProgress.setValue(value);
        setVolumeLabel(value);
        getMediaPlayer().setVolume(value);
    }

    private void initVideoFilesPath(String videoFolder) {
        videos = FileUtils.readFilePath(videoFolder);
        videos.sort(Comparator.naturalOrder());
        videoIndex = 0;
        // System.out.println(videos);
    }

    private void loading() {
        if (videos.isEmpty()) {
            return;
        }
        String path = videos.get(videoIndex);
        setTitle("VideoPlayer-" + FileUtils.getFileName(path) + "（预加载）");
    }

    private void initPlay() {
        if (videos.isEmpty()) {
            return;
        }
        getMediaPlayer().playMedia(videos.get(videoIndex));
        setWindowTitle();
        pauseButton.setLabel("pause");
        setProgress(getMediaPlayer().getTime(), getMediaPlayer().getLength());
        progressTimer.start();
        continueTimer.start();
        this.firstPlay = false;
    }

    private void play() {
        if (videos.isEmpty()) {
            return;
        }
        getMediaPlayer().playMedia(videos.get(videoIndex));
        setWindowTitle();
    }

    private void setWindowTitle() {
        String title = getMediaPlayer().getMediaMeta().getTitle();
        setTitle("VideoPlayer-" + title);
    }

    private String getTimeString(long curr, long total) {
        return formatSecond2Time(curr) + " / " + formatSecond2Time(total);
    }

    private String getTimeString() {
        setTotalTime();
        return formatSecond2Time(getMediaPlayer().getTime()) + " / " + TOTAL_TIME;
    }

    private void setTotalTime() {
        if (TOTAL_TIME == null) {
            long totalSecond = getMediaPlayer().getLength();
            TOTAL_TIME = formatSecond2Time(totalSecond);
        }
    }

    private String formatSecond2Time(long milliseconds) {
        int second = (int) (milliseconds / 1000);
        int h = second / 3600;
        int m = (second % 3600) / 60;
        int s = (second % 3600) % 60;
        return String.format("%02d", h) + ":" + String.format("%02d", m) + ":"
                + String.format("%02d", s);
    }

    private Timer getContinueTimer() {
        return new Timer(1000, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long total = getMediaPlayer().getLength();
                long curr = getMediaPlayer().getTime();
                if (curr == total) {
                    videoIndex++;
                    if (videoIndex >= videos.size()) {
                        continueTimer.stop();
                        System.out.println("all videos finished...");
                        return;
                    }
                    getMediaPlayer().playMedia(videos.get(videoIndex));
                    setWindowTitle();
                    setProgress(getMediaPlayer().getTime(), getMediaPlayer().getLength());
                    progressTimer.restart();
                }
            }
        });
    }

    private Timer getProgressTimer() {
        return new Timer(1000, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (getProgress().getValue() >= PROGRESS_MAX_VALUE) {
                    // 结束定时器
                    progressTimer.stop();
                    return;
                }
                // 设置进度值
                setProgress(getMediaPlayer().getTime(), getMediaPlayer().getLength());
            }
        });
    }

    private void setProgress(long curr, long total) {
        float percent = (float) curr / total;
        int value = (int) (percent * 100);
        getProgress().setValue(value);
        displayTime.setText(getTimeString(curr, total));
    }

    private WindowAdapter windowStateChangedResetProgressWidth() {
        return new WindowAdapter() {
            @Override
            public void windowStateChanged(WindowEvent state) {
                // state=1或7为最小化，此处不处理

                if (state.getNewState() == 0) {
                    // System.out.println("窗口恢复到初始状态");
                    setProgressWidthAutoAdaptWindow();
                    setListWindowInvisible();
                    setListWindowBounds();
                } else if (state.getNewState() == 6) {
                    // System.out.println("窗口最大化");
                    setProgressWidthAutoAdaptWindow();
                    setListWindowInvisible();
                    setListWindowBounds();
                }
            }
        };
    }

    private void setListWindowInvisible() {
        if (listWindow != null && listWindow.isVisible()) {
            listWindow.setVisible(false);
        }
    }

    private void setProgressWidthAutoAdaptWindow() {
        getProgress().setPreferredSize(getNewDimension());
    }

    private ComponentAdapter windowResizedResetProgressWidth() {
        return new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                setProgressWidthAutoAdaptWindow();
                setListWindowInvisible();
            }
        };
    }

    private Dimension getNewDimension() {
        return new Dimension(getWidth(), PROGRESS_HEIGHT);
    }

    private MouseAdapter setVideoPlayPoint() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int x = e.getX();
                long total = getMediaPlayer().getLength();
                long time = (long) ((float) x / progress.getWidth() * total);
                setProgress(time, total);
                getMediaPlayer().setTime(time);
            }
        };
    }

    private FocusAdapter videoSurfaceFocusAction() {
        return new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                setPauseButtonAsDefaultFocus();
            }
        };
    }

    private WindowAdapter closeWindowReleaseMedia() {
        return new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                getMediaPlayer().stop();
                getMediaPlayer().release();
                System.exit(0);
            }
        };
    }

    private MouseListener mouseClickedResetSpeed() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (speed == 1.0f) {
                    return;
                }
                speed = 1.0f;
                getMediaPlayer().setRate(speed);
                displaySpeed.setText("x" + speed);
            }
        };
    }

    private MouseListener mouseClickedFastForward() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (speed >= 3.0f) {
                    speed = 1.0f;
                } else {
                    speed += 0.5f;
                }
                getMediaPlayer().setRate(speed);
                displaySpeed.setText("x" + speed);
            }
        };
    }

    private MouseAdapter mouseClickedMediaPause() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (videos.isEmpty()) {
                    return;
                }
                if (firstPlay) {
                    initPlay();
                    return;
                }
                setMediaStatusAndPauseButton();
                if (progressTimer.isRunning()) {
                    progressTimer.stop();
                } else {
                    progressTimer.restart();
                }
            }
        };
    }

    private void setMediaStatusAndPauseButton() {
        if (getMediaPlayer().isPlaying()) {
            getMediaPlayer().pause();
            pauseButton.setLabel("play");
        } else {
            getMediaPlayer().play();
            pauseButton.setLabel("pause");
        }
    }

    private WindowFocusListener getWindowFocusListener() {
        return new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                setPauseButtonAsDefaultFocus();
            }

            @Override
            public void windowGainedFocus(WindowEvent e) {
                setPauseButtonAsDefaultFocus();
            }

            @Override
            public void windowLostFocus(WindowEvent e) {}
        };
    }

    private void setPauseButtonAsDefaultFocus() {
        pauseButton.requestFocus();
    }

    private KeyListener spaceKeyPressMediaPause() {
        return new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (videos.isEmpty()) {
                    return;
                }
                if (firstPlay) {
                    initPlay();
                    return;
                }
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    setMediaStatusAndPauseButton();
                }
            }
        };
    }

    private JProgressBar getProgress() {
        return progress;
    }

    private EmbeddedMediaPlayer getMediaPlayer() {
        return mediaPlayerComponent.getMediaPlayer();
    }

    private Canvas getVideoSurface() {
        return mediaPlayerComponent.getVideoSurface();
    }

}
