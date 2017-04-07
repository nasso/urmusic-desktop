package io.github.nasso.urmusic.ui;

import java.text.NumberFormat;

import io.github.nasso.urmusic.Urmusic;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Callback;

public class SizeDialog extends Dialog<int[]> {
	public SizeDialog(String title, int w, int h) {
		this.getDialogPane().setBackground(Urmusic.PANES_BACKGROUND_FLAT);
		
		NumberTextField widthField = new NumberTextField(NumberFormat.getIntegerInstance(), 1, Integer.MAX_VALUE);
		widthField.setValue(w);
		NumberTextField heightField = new NumberTextField(NumberFormat.getIntegerInstance(), 1, Integer.MAX_VALUE);
		heightField.setValue(h);
		
		Label titleLabel = new Label(title);
		titleLabel.setFont(Font.font(24));
		titleLabel.setTextFill(Color.WHITE);
		Label widthLabel = new Label("Width:");
		widthLabel.setTextFill(Color.WHITE);
		Label heightLabel = new Label("Height:");
		heightLabel.setTextFill(Color.WHITE);
		
		GridPane gridPane = new GridPane();
		gridPane.setHgap(4);
		gridPane.setVgap(4);
		gridPane.setAlignment(Pos.TOP_CENTER);
		gridPane.add(titleLabel, 0, 0, 2, 1);
		gridPane.add(widthLabel, 0, 1);
		gridPane.add(widthField, 1, 1);
		gridPane.add(heightLabel, 0, 2);
		gridPane.add(heightField, 1, 2);
		
		this.getDialogPane().setContent(gridPane);
		
		this.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
		
		this.setResultConverter(new Callback<ButtonType, int[]>() {
			public int[] call(ButtonType param) {
				if(param == ButtonType.OK) return new int[] { Integer.parseInt(widthField.getText()), Integer.parseInt(heightField.getText()) };
				
				return null;
			}
		});
	}
}
