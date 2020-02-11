package LegoScorer;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

public class TimeField extends TextField {
	
	public TimeField(String str) {
		
		super(str);
		
		setTextFormatter(new TextFormatter<String>(field -> {
			
			String text = field.getControlNewText();
			String newText = "";
			
			for(int i = 0;i < text.length();i++) {
				if(newText.length() == 2) newText += ':';
				
				if(Character.isDigit(text.charAt(i)))
					newText += text.charAt(i);
				
				if(newText.length() == 5) break;
			}
			
			while(newText.length() < 5) {
				if(newText.length() == 2) newText += ':';
				newText += '0';
			}
			
			if(Integer.valueOf("" + newText.charAt(0) + newText.charAt(1)) > 23) {
				newText = "23" + newText.substring(2);
			}
			if(Integer.valueOf("" + newText.charAt(3) + newText.charAt(4)) > 59) {
				newText = newText.substring(0, 3) + "00";
			}
			
			field.setRange(0, 5);
			field.setText(newText);
			
			if(field.getCaretPosition() == 2 && field.getControlText().charAt(1) != newText.charAt(1)) {
				field.setCaretPosition(3);
				field.setAnchor(3);
			}
			return field;
		}));
	}
	
	public int getValue() {
		String textVal = getText();
		return Integer.valueOf("" + textVal.charAt(0) + textVal.charAt(1) + textVal.charAt(3) + textVal.charAt(4));
	}
	
}
