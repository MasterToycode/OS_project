import javax.swing.*;
import java.io.IOException;

public class Main {
    public static void main(String[] args)
    {
        try {
            OSKernel.fileUtils.LoadJobs();  //从文件读取作业并加载到后备队列中
        } catch (IOException e) {
            System.out.println("作业加载失败，请检查！");
            throw new RuntimeException(e);
        }
        SwingUtilities.invokeLater(() -> {
            OSKernel.ui.createAndShowGUI(); // 启动UI界面
        });
    }
}
