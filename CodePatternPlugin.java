import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;

public class CodePatternPlugin implements Plugin{

	@Override
	public void call(JavacTask task, String[] args) {
		System.out.println("Running!");
		task.setTaskListener(new CodePatternTaskListener(task));
		
	}

	@Override
	public String getName() {
		return "CodePatternPlugin";
	}

}
