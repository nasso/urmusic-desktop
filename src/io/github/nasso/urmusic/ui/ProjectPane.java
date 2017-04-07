package io.github.nasso.urmusic.ui;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.util.List;

import io.github.nasso.urmusic.ApplicationPreferences;
import io.github.nasso.urmusic.Project;
import io.github.nasso.urmusic.Urmusic;
import io.github.nasso.urmusic.Utils;
import io.github.nasso.urmusic.core.AdvancedSettings;
import io.github.nasso.urmusic.core.ImageSection;
import io.github.nasso.urmusic.core.Section;
import io.github.nasso.urmusic.core.SectionGroup;
import io.github.nasso.urmusic.core.SectionGroupElement;
import io.github.nasso.urmusic.core.SectionTarget;
import io.github.nasso.urmusic.core.SectionType;
import io.github.nasso.urmusic.core.Settings;
import io.github.nasso.urmusic.expression.ExpressionProperty;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.StringConverter;

public class ProjectPane extends Pane {
	private FileChooser imageFileChooser;
	
	private class NamedProperty {
		public String name;
		public Object obj;
		public Object objInst;
		public TreeItem<NamedProperty> thisItem;
		public Runnable onvaluechanged;
		
		public NamedProperty(Object obj) {
			this(obj.getClass().getSimpleName(), obj);
		}
		
		public NamedProperty(String name, Object obj) {
			this.name = name;
			this.obj = obj;
		}
		
		public NamedProperty(String name, Object obj, Object objInst) {
			this.name = name;
			this.obj = obj;
			this.objInst = objInst;
		}
	}
	
	private class PairValueCell extends TreeTableCell<NamedProperty, Object> {
		protected void updateItem(Object item, boolean empty) {
			super.updateItem(item, empty);
			
			this.setText(null);
			this.setGraphic(null);
			
			if(!(empty || item == null) && item != null && item instanceof NamedProperty) {
				NamedProperty prop = (NamedProperty) item;
				
				this.setFont(ProjectPane.this.ubuntuMonoR);
				
				if(prop.obj instanceof SectionGroupElement) {
					SectionGroupElement sge = (SectionGroupElement) prop.obj;
					
					TextField tf = new TextField(sge.name);
					if(sge instanceof Section) tf.setPromptText("Section name");
					else if(sge instanceof SectionGroup) tf.setPromptText("Group name");
					tf.setFont(ProjectPane.this.ubuntuMonoR);
					tf.textProperty().addListener((e) -> {
						tf.setStyle("-fx-text-fill: #f77;");
					});
					tf.setOnAction((ae) -> {
						tf.setStyle("-fx-text-fill: #fff;");
						sge.name = tf.getText();
					});
					this.setGraphic(tf);
				} else if(prop.obj instanceof SectionTarget) {
					SectionTarget st = (SectionTarget) prop.obj;
					
					Section sect = (Section) prop.objInst;
					
					ComboBox<SectionType> cbox = new ComboBox<SectionType>();
					SectionType[] consts = SectionType.class.getEnumConstants();
					
					for(SectionType o : consts) {
						cbox.getItems().add(o);
					}
					
					try {
						cbox.setValue((SectionType) st.getClass().getField("THIS_TYPE").get(null));
					} catch(IllegalArgumentException e1) {
						e1.printStackTrace();
					} catch(IllegalAccessException e1) {
						e1.printStackTrace();
					} catch(NoSuchFieldException e1) {
						e1.printStackTrace();
					} catch(SecurityException e1) {
						e1.printStackTrace();
					}
					
					cbox.valueProperty().addListener((ae) -> {
						try {
							sect.type = cbox.getValue();
							sect.updateType();
							prop.thisItem.getChildren().clear();
							prop.obj = sect.target;
							ProjectPane.this.addSectionTargetGroupElementTreeItem(sect.target, prop.thisItem);
						} catch(IllegalArgumentException e) {
							e.printStackTrace();
						}
					});
					
					this.setGraphic(cbox);
				} else if(prop.obj instanceof Field) {
					Field f = (Field) prop.obj;
					
					try {
						Object val = f.get(prop.objInst);
						
						if(val instanceof ExpressionProperty) {
							ExpressionProperty ep = (ExpressionProperty) val;
							TextField tf = new TextField(ep.getExpr());
							tf.setPromptText("Expression");
							tf.setFont(ProjectPane.this.ubuntuMonoR);
							tf.textProperty().addListener((e) -> {
								tf.setStyle("-fx-text-fill: #f77;");
							});
							tf.setOnAction((ae) -> {
								tf.setStyle("-fx-text-fill: #fff;");
								ep.setExpr(tf.getText());
								if(prop.onvaluechanged != null) prop.onvaluechanged.run();
							});
							this.setGraphic(tf);
						} else if(val instanceof Boolean) {
							CheckBox checkBox = new CheckBox();
							checkBox.setSelected((boolean) val);
							checkBox.setOnAction((ae) -> {
								try {
									f.set(prop.objInst, checkBox.isSelected());
									if(prop.onvaluechanged != null) prop.onvaluechanged.run();
								} catch(IllegalArgumentException e) {
									e.printStackTrace();
								} catch(IllegalAccessException e) {
									e.printStackTrace();
								}
							});
							this.setGraphic(checkBox);
						} else if(val instanceof Color) {
							ColorPicker colorPicker = new ColorPicker((Color) val);
							colorPicker.setOnAction((ae) -> {
								try {
									f.set(prop.objInst, colorPicker.getValue());
									if(prop.onvaluechanged != null) prop.onvaluechanged.run();
								} catch(IllegalArgumentException e) {
									e.printStackTrace();
								} catch(IllegalAccessException e) {
									e.printStackTrace();
								}
							});
							this.setGraphic(colorPicker);
						} else if(val instanceof String) {
							TextField tf = new TextField(val.toString());
							tf.setPromptText("Text");
							tf.setFont(ProjectPane.this.ubuntuMonoR);
							tf.textProperty().addListener((e) -> {
								tf.setStyle("-fx-text-fill: #f77;");
							});
							tf.setOnAction((ae) -> {
								tf.setStyle("-fx-text-fill: #fff;");
								
								try {
									f.set(prop.objInst, tf.getText());
									if(prop.onvaluechanged != null) prop.onvaluechanged.run();
								} catch(IllegalArgumentException e1) {
									e1.printStackTrace();
								} catch(IllegalAccessException e1) {
									e1.printStackTrace();
								}
							});
							this.setGraphic(tf);
						} else if(val instanceof Float) {
							TextField tf = new TextField(val.toString());
							tf.setPromptText("Number");
							tf.setFont(ProjectPane.this.ubuntuMonoR);
							tf.textProperty().addListener((e) -> {
								tf.setStyle("-fx-text-fill: #f77;");
							});
							tf.setOnAction((ae) -> {
								tf.setStyle("-fx-text-fill: #fff;");
								
								try {
									try {
										float floatValue = Float.valueOf(tf.getText());
										f.set(prop.objInst, floatValue);
										if(prop.onvaluechanged != null) prop.onvaluechanged.run();
									} catch(NumberFormatException e) {
										tf.setText(f.get(prop.objInst).toString());
										tf.setStyle("-fx-text-fill: #fff;");
									}
								} catch(IllegalArgumentException e1) {
									e1.printStackTrace();
								} catch(IllegalAccessException e1) {
									e1.printStackTrace();
								}
							});
							this.setGraphic(tf);
						} else if(val instanceof Integer) {
							TextField tf = new TextField(val.toString());
							tf.setPromptText("Integer");
							tf.setFont(ProjectPane.this.ubuntuMonoR);
							tf.textProperty().addListener((e) -> {
								tf.setStyle("-fx-text-fill: #f77;");
							});
							tf.setOnAction((ae) -> {
								tf.setStyle("-fx-text-fill: #fff;");
								
								try {
									try {
										int intValue = Integer.valueOf(tf.getText());
										f.set(prop.objInst, intValue);
										if(prop.onvaluechanged != null) prop.onvaluechanged.run();
									} catch(NumberFormatException e) {
										tf.setText(f.get(prop.objInst).toString());
										tf.setStyle("-fx-text-fill: #fff;");
										if(prop.onvaluechanged != null) prop.onvaluechanged.run();
									}
								} catch(IllegalArgumentException e1) {
									e1.printStackTrace();
								} catch(IllegalAccessException e1) {
									e1.printStackTrace();
								}
							});
							this.setGraphic(tf);
						} else if(val != null && val.getClass().isEnum()) {
							ComboBox<Object> cbox = new ComboBox<Object>();
							Class<?> clss = val.getClass();
							Object[] consts = clss.getEnumConstants();
							
							for(Object o : consts) {
								cbox.getItems().add(o);
							}
							
							cbox.setValue(val);
							
							cbox.valueProperty().addListener((ae) -> {
								try {
									f.set(prop.objInst, cbox.getValue());
									if(prop.onvaluechanged != null) prop.onvaluechanged.run();
								} catch(IllegalArgumentException e) {
									e.printStackTrace();
								} catch(IllegalAccessException e) {
									e.printStackTrace();
								}
							});
							
							this.setGraphic(cbox);
						} else if(val instanceof Image || f.getType() == Image.class) {
							ImageSection targ = (ImageSection) prop.objInst;
							
							BorderPane borderPane = new BorderPane();
							Label label = new Label(targ.getImageURL());
							Button btn = new Button("...");
							Button x = new Button("x");
							
							label.setTextAlignment(TextAlignment.LEFT);
							
							borderPane.setLeft(btn);
							borderPane.setCenter(label);
							borderPane.setRight(x);
							
							btn.setOnAction((ae) -> {
								File file = ProjectPane.this.imageFileChooser.showOpenDialog(Urmusic.getWindow());
								if(file != null) {
									try {
										targ.setImageURL(file.toURI().toURL().toString());
										label.setText(targ.getImageURL());
										if(prop.onvaluechanged != null) prop.onvaluechanged.run();
									} catch(MalformedURLException e) {
										e.printStackTrace();
									}
								}
							});
							
							x.setOnAction((ae) -> {
								label.setText("NULL");
								targ.setImageURL(null);
								if(prop.onvaluechanged != null) prop.onvaluechanged.run();
							});
							
							this.setGraphic(borderPane);
						} else {
							if(val == null) this.setText("NULL (" + f.getType().getSimpleName() + ")");
							else this.setText("N/A for " + val.getClass().getSimpleName());
							this.setGraphic(null);
						}
					} catch(IllegalArgumentException e) {
						e.printStackTrace();
					} catch(IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	private Project project;
	private Canvas cvs = new Canvas();
	
	private TreeTableView<NamedProperty> treeView;
	
	private TreeItem<NamedProperty> draggedItem;
	
	private Pane treePane;
	private Pane separatorPane;
	private Pane canvasPane;
	
	private Font ubuntuMonoR;
	
	private NamedProperty contextMenuTarget = null;
	private ContextMenu ctxMenuSectionGroup, ctxMenuSection;
	
	public ProjectPane(Project p) {
		this.project = p;
		
		this.ubuntuMonoR = Font.loadFont(this.getClass().getResourceAsStream("/res/UbuntuMono-R.ttf"), 14);
		
		this.imageFileChooser = new FileChooser();
		this.imageFileChooser.getExtensionFilters().add(new ExtensionFilter("Image files", "*.png", "*.jpg", "*.jpeg", "*.bmp"));
		
		this.createContextMenus();
		this.createSeparatorPane();
		this.createTreePane();
		this.createCanvasPane();
		
		this.getChildren().addAll(this.treePane, this.separatorPane, this.canvasPane);
	}
	
	private SectionGroup searchParent(SectionGroupElement sge, SectionGroup root) {
		List<SectionGroupElement> children = root.getUnmodifiableChildren();
		
		if(children.contains(sge)) return root;
		
		for(SectionGroupElement elem : children) {
			if(elem instanceof SectionGroup) {
				SectionGroup parent = this.searchParent(sge, (SectionGroup) elem);
				if(parent != null) return parent;
			}
		}
		
		return null;
	}
	
	private TreeItem<NamedProperty> searchItem(SectionGroupElement sge, TreeItem<NamedProperty> root) {
		NamedProperty prop = root.getValue();
		if(prop.obj == sge) return root;
		
		for(TreeItem<NamedProperty> item : root.getChildren()) {
			TreeItem<NamedProperty> i = this.searchItem(sge, item);
			if(i != null) return i;
		}
		
		return null;
	}
	
	private void createContextMenus() {
		this.ctxMenuSectionGroup = new ContextMenu();
		this.ctxMenuSection = new ContextMenu();
		
		MenuItem addSectionItem = new MenuItem("Add a section");
		addSectionItem.setOnAction((ae) -> {
			if(!(this.contextMenuTarget.obj instanceof SectionGroup)) {
				System.err.println("Trying to add a section to: " + this.contextMenuTarget.obj + (this.contextMenuTarget.obj == null ? " (NULL)" : " (" + this.contextMenuTarget.obj.getClass().getSimpleName() + ")"));
			}
			
			SectionGroup group = (SectionGroup) this.contextMenuTarget.obj;
			Section s = new Section();
			group.addChild(s);
			
			this.addSectionGroupElementTreeItem(s, this.searchItem(group, this.treeView.getRoot()));
		});
		
		MenuItem addSectionGroupItem = new MenuItem("Add a section group");
		addSectionGroupItem.setOnAction((ae) -> {
			if(!(this.contextMenuTarget.obj instanceof SectionGroup)) {
				System.err.println("Trying to add a section group to: " + this.contextMenuTarget.obj + (this.contextMenuTarget.obj == null ? " (NULL)" : " (" + this.contextMenuTarget.obj.getClass().getSimpleName() + ")"));
			}
			
			SectionGroup group = (SectionGroup) this.contextMenuTarget.obj;
			SectionGroup sg = new SectionGroup();
			group.addChild(sg);
			
			TreeItem<NamedProperty> item = this.searchItem(group, this.treeView.getRoot());
			if(item == null) {
				System.err.println("Couldn't find '" + group + "' in the tree.");
				return;
			}
			this.addSectionGroupElementTreeItem(sg, item);
		});
		
		MenuItem deleteItem = new MenuItem("Delete");
		deleteItem.setOnAction((ae) -> {
			SectionGroupElement sge = (SectionGroupElement) this.contextMenuTarget.obj;
			
			SectionGroup sg = this.searchParent(sge, this.project.getSettings().rootGroup);
			
			if(sg == null) System.err.println("Can't delete because couldn't find the thing");
			else {
				TreeItem<NamedProperty> item = this.searchItem(sg, this.treeView.getRoot());
				TreeItem<NamedProperty> itemChild = this.searchItem(sge, item);
				item.getChildren().remove(itemChild);
				
				sg.removeChild(sge);
			}
		});
		MenuItem deleteItemGroup = new MenuItem("Delete");
		deleteItemGroup.setOnAction(deleteItem.getOnAction());
		
		MenuItem cloneItem = new MenuItem("Clone");
		cloneItem.setOnAction((ae) -> {
			SectionGroupElement sge = (SectionGroupElement) this.contextMenuTarget.obj;
			
			SectionGroup sg = this.searchParent(sge, this.project.getSettings().rootGroup);
			
			if(sg == null) System.err.println("Can't clone because couldn't find the thing");
			else {
				TreeItem<NamedProperty> item = this.searchItem(sg, this.treeView.getRoot());
				
				SectionGroupElement clonedSGE = null;
				if(sge instanceof Section) {
					clonedSGE = new Section((Section) sge);
				} else if(sge instanceof SectionGroup) {
					clonedSGE = new SectionGroup((SectionGroup) sge);
				}
				
				if(clonedSGE != null) { // Should always be true
					sg.addChild(clonedSGE);
					this.addSectionGroupElementTreeItem(clonedSGE, item);
				}
			}
		});
		MenuItem cloneItemGroup = new MenuItem("Clone");
		cloneItemGroup.setOnAction(cloneItem.getOnAction());
		
		this.ctxMenuSectionGroup.getItems().addAll(addSectionGroupItem, addSectionItem, cloneItemGroup, deleteItemGroup);
		this.ctxMenuSection.getItems().addAll(cloneItem, deleteItem);
	}
	
	private void createSeparatorPane() {
		this.separatorPane = new Pane();
		this.separatorPane.setStyle("-fx-background-color: linear-gradient(to right, #333, #444);");
		this.separatorPane.setLayoutX(ApplicationPreferences.treePanelSize);
		this.separatorPane.setLayoutY(0);
		this.separatorPane.setPrefWidth(4);
		this.separatorPane.prefHeightProperty().bind(this.heightProperty());
		
		this.separatorPane.setCursor(Cursor.H_RESIZE);
		this.separatorPane.setOnMouseDragged((e) -> {
			this.separatorPane.setLayoutX(Utils.clamp(this.getLayoutX() + this.separatorPane.getLayoutX() + e.getX(), 50, this.getWidth() - 54));
		});
	}
	
	private TreeItem<NamedProperty> createTreeRootItem(SectionGroup root, TreeItem<NamedProperty> rootItem) {
		if(rootItem == null) rootItem = new TreeItem<NamedProperty>(new NamedProperty(root));
		
		try {
			Field[] fields = SectionGroup.class.getFields();
			for(Field f : fields) {
				if(Modifier.isStatic(f.getModifiers()) || f.getName().equals("name")) continue;
				
				if(SectionTarget.class.isAssignableFrom(f.getType())) {
					SectionTarget target;
					target = (SectionTarget) f.get(root);
					
					NamedProperty prop = new NamedProperty(f.getName(), target, root);
					TreeItem<NamedProperty> targItem = new TreeItem<NamedProperty>(prop);
					prop.thisItem = targItem;
					
					this.addSectionTargetGroupElementTreeItem(target, targItem);
					
					rootItem.getChildren().add(targItem);
				} else if(!SectionType.class.isAssignableFrom(f.getType()) || Color.class.isAssignableFrom(f.getType())) {
					rootItem.getChildren().add(new TreeItem<NamedProperty>(new NamedProperty(f.getName(), f, root)));
				}
			}
		} catch(IllegalArgumentException e1) {
			e1.printStackTrace();
		} catch(IllegalAccessException e1) {
			e1.printStackTrace();
		}
		
		for(SectionGroupElement e : root.getUnmodifiableChildren()) {
			this.addSectionGroupElementTreeItem(e, rootItem);
		}
		
		return rootItem;
	}
	
	private TreeItem<NamedProperty> addSectionGroupElementTreeItem(SectionGroupElement e, TreeItem<NamedProperty> rootItem) {
		TreeItem<NamedProperty> item = new TreeItem<NamedProperty>(new NamedProperty(e));
		
		try {
			Field[] fields = e.getClass().getFields();
			for(Field f : fields) {
				if(Modifier.isStatic(f.getModifiers()) || f.getName().equals("name")) continue;
				
				if(SectionTarget.class.isAssignableFrom(f.getType())) {
					SectionTarget target = (SectionTarget) f.get(e);
					
					NamedProperty prop = new NamedProperty(f.getName(), target, e);
					TreeItem<NamedProperty> targItem = new TreeItem<NamedProperty>(prop);
					prop.thisItem = targItem;
					
					this.addSectionTargetGroupElementTreeItem(target, targItem);
					
					item.getChildren().add(targItem);
				} else if(!SectionType.class.isAssignableFrom(f.getType()) || Color.class.isAssignableFrom(f.getType())) {
					item.getChildren().add(new TreeItem<NamedProperty>(new NamedProperty(f.getName(), f, e)));
				}
			}
		} catch(IllegalArgumentException e1) {
			e1.printStackTrace();
		} catch(IllegalAccessException e1) {
			e1.printStackTrace();
		}
		
		if(e instanceof SectionGroup) {
			for(SectionGroupElement sge : ((SectionGroup) e).getUnmodifiableChildren()) {
				this.addSectionGroupElementTreeItem(sge, item);
			}
		}
		
		rootItem.getChildren().add(item);
		
		return rootItem;
	}
	
	private void addSectionTargetGroupElementTreeItem(SectionTarget target, TreeItem<NamedProperty> targItem) {
		Field[] targFields = target.getClass().getFields();
		for(Field targF : targFields) {
			if(Modifier.isStatic(targF.getModifiers())) continue;
			
			targItem.getChildren().add(new TreeItem<NamedProperty>(new NamedProperty(targF.getName(), targF, target)));
		}
	}
	
	private boolean isChildOf(TreeItem<NamedProperty> child, TreeItem<NamedProperty> parent) {
		if(child.getParent() == parent) return true;
		if(child == parent || child.getParent() == null) return false;
		
		return this.isChildOf(child.getParent(), parent);
	}
	
	private void createTreePane() {
		this.treePane = new Pane();
		
		TreeTableColumn<NamedProperty, String> colName = new TreeTableColumn<NamedProperty, String>();
		colName.setText("Name");
		colName.setEditable(false);
		colName.setSortable(false);
		colName.setResizable(false);
		colName.setCellFactory((param) -> {
			TextFieldTreeTableCell<NamedProperty, String> cell = new TextFieldTreeTableCell<NamedProperty, String>();
			
			cell.setConverter(new StringConverter<String>() {
				public String toString(String object) {
					NamedProperty p = cell.getTreeTableRow().getItem();
					if(p != null) return p.name;
					
					return "undefined";
				}
				
				public String fromString(String string) {
					return string;
				}
			});
			
			return cell;
		});
		
		TreeTableColumn<NamedProperty, Object> colValue = new TreeTableColumn<NamedProperty, Object>();
		colValue.getStyleClass().add("colValue");
		colValue.setText("Value");
		colValue.setEditable(true);
		colValue.setSortable(false);
		colValue.setResizable(false);
		colValue.setCellValueFactory((data) -> {
			Object value = data.getValue().getValue();
			return new ReadOnlyObjectWrapper<Object>(value);
		});
		
		colValue.setCellFactory((param) -> {
			return new PairValueCell();
		});
		
		this.treeView = new TreeTableView<NamedProperty>();
		this.treeView.setEditable(true);
		this.treeView.getColumns().add(colName);
		this.treeView.getColumns().add(colValue);
		this.treeView.setTableMenuButtonVisible(false);
		this.treeView.setOnMouseDragged(null);
		this.treeView.setRowFactory((ttview) -> {
			TreeTableRow<NamedProperty> row = new TreeTableRow<NamedProperty>() {
				public void updateItem(NamedProperty p, boolean empty) {
					super.updateItem(p, empty);
					
					if(empty) {
						this.setContextMenu(null);
					} else if(p != null) {
						if(p.obj instanceof SectionGroup) {
							this.setContextMenu(ProjectPane.this.ctxMenuSectionGroup);
						} else if(p.obj instanceof Section) {
							this.setContextMenu(ProjectPane.this.ctxMenuSection);
						} else {
							this.setContextMenu(null);
						}
					}
				}
			};
			
			row.setOnContextMenuRequested((e) -> {
				this.contextMenuTarget = row.getItem();
			});
			
			row.setOnDragDetected((e) -> {
				TreeItem<NamedProperty> selected = this.treeView.getSelectionModel().getSelectedItem();
				
				if(selected != null && selected.getValue().obj instanceof SectionGroupElement && selected.getParent().getParent() != null) {
					Dragboard db = this.treeView.startDragAndDrop(TransferMode.MOVE);
					
					db.setDragView(row.snapshot(null, null));
					
					ClipboardContent content = new ClipboardContent();
					content.putString("HI YOU");
					db.setContent(content);
					
					this.draggedItem = selected;
					
					e.consume();
				}
			});
			
			row.setOnDragOver((e) -> {
				Dragboard db = e.getDragboard();
				
				if(db.hasString()) {
					e.acceptTransferModes(TransferMode.MOVE);
				}
				
				e.consume();
			});
			
			row.setOnDragDropped((e) -> {
				if(this.draggedItem == null || this.draggedItem == row.getTreeItem()) return;
				
				NamedProperty prop = row.getTreeItem().getValue();
				if(prop.obj instanceof Field) {
					if(!(prop.objInst instanceof SectionGroup)) return;
					else {
						TreeItem<NamedProperty> source = this.draggedItem;
						TreeItem<NamedProperty> dest = row.getTreeItem();
						TreeItem<NamedProperty> sourceParent = source.getParent();
						TreeItem<NamedProperty> destParent = dest.getParent();
						
						if(this.isChildOf(dest, source)) return;
						
						SectionGroupElement sourceElem = (SectionGroupElement) source.getValue().obj;
						SectionGroup sourceParentGroup = (SectionGroup) sourceParent.getValue().obj;
						SectionGroup destParentGroup = (SectionGroup) destParent.getValue().obj;
						
						int index = 0;
						
						sourceParentGroup.removeChild(sourceElem);
						destParentGroup.addChild(index, sourceElem);
						
						int itemIndex = 0;
						ObservableList<TreeItem<NamedProperty>> destParentChildren = destParent.getChildren();
						for(; itemIndex < destParentChildren.size() && destParentChildren.get(itemIndex).getValue().obj instanceof Field; itemIndex++) {
						}
						
						sourceParent.getChildren().remove(source);
						destParent.getChildren().add(itemIndex, source);
						return;
					}
				} else if(!(prop.obj instanceof SectionGroupElement)) return;
				
				boolean success = false;
				if(e.getDragboard().hasString()) {
					if(!row.isEmpty()) {
						TreeItem<NamedProperty> source = this.draggedItem;
						TreeItem<NamedProperty> dest = row.getTreeItem();
						TreeItem<NamedProperty> sourceParent = source.getParent();
						TreeItem<NamedProperty> destParent = dest.getParent();
						
						if(this.isChildOf(dest, source)) return;
						
						SectionGroupElement sourceElem = (SectionGroupElement) source.getValue().obj;
						SectionGroupElement destElem = (SectionGroupElement) dest.getValue().obj;
						SectionGroup sourceParentGroup = (SectionGroup) sourceParent.getValue().obj;
						SectionGroup destParentGroup = (SectionGroup) destParent.getValue().obj;
						
						int index = destParentGroup.getUnmodifiableChildren().indexOf(destElem);
						
						sourceParentGroup.removeChild(sourceElem);
						destParentGroup.addChild(index, sourceElem);
						
						int itemIndex = destParent.getChildren().indexOf(dest);
						sourceParent.getChildren().remove(source);
						destParent.getChildren().add(itemIndex, source);
						
						success = true;
					}
				}
				
				this.draggedItem = null;
				e.setDropCompleted(success);
				e.consume();
			});
			
			return row;
		});
		
		colName.prefWidthProperty().bind(this.treeView.widthProperty().divide(2).subtract(1));
		colValue.prefWidthProperty().bind(this.treeView.widthProperty().divide(2).subtract(1));
		
		TreeItem<NamedProperty> sectionRoot = this.createTreeRootItem(this.project.getSettings().rootGroup, null);
		TreeItem<NamedProperty> settingsRoot = new TreeItem<NamedProperty>();
		TreeItem<NamedProperty> advancedSettingsRoot = new TreeItem<NamedProperty>();
		
		// Add the settings root
		Settings set = this.project.getSettings();
		settingsRoot.setValue(new NamedProperty(set));
		Field[] fields = Settings.class.getFields();
		for(Field f : fields) {
			if(Modifier.isStatic(f.getModifiers()) || f.getType() == SectionGroup.class || f.getType() == AdvancedSettings.class) continue;
			
			settingsRoot.getChildren().add(new TreeItem<NamedProperty>(new NamedProperty(f.getName(), f, set)));
		}
		
		TreeItem<NamedProperty> projNameItem;
		try {
			NamedProperty prop = new NamedProperty("name", Project.class.getField("name"), this.project);
			prop.onvaluechanged = () -> {
				if(this.project.getOnNameChanged() != null) this.project.getOnNameChanged().run();
			};
			
			projNameItem = new TreeItem<NamedProperty>(prop);
			settingsRoot.getChildren().add(projNameItem);
		} catch(NoSuchFieldException e) {
			e.printStackTrace();
		} catch(SecurityException e) {
			e.printStackTrace();
		}
		
		settingsRoot.getChildren().add(sectionRoot);
		
		// Add advanced settings
		AdvancedSettings advSet = set.advanced;
		advancedSettingsRoot.setValue(new NamedProperty(advSet));
		fields = AdvancedSettings.class.getFields();
		for(Field f : fields) {
			if(Modifier.isStatic(f.getModifiers())) continue;
			
			advancedSettingsRoot.getChildren().add(new TreeItem<NamedProperty>(new NamedProperty(f.getName(), f, advSet)));
		}
		
		settingsRoot.getChildren().add(advancedSettingsRoot);
		
		this.treeView.setBackground(null);
		this.treeView.setRoot(settingsRoot);
		this.treeView.setShowRoot(false);
		this.treeView.setLayoutX(0);
		this.treeView.setLayoutY(0);
		this.treeView.prefHeightProperty().bind(this.treePane.heightProperty());
		this.treeView.prefWidthProperty().bind(this.treePane.widthProperty());
		
		this.treePane.setBackground(Urmusic.PANES_BACKGROUND_FLAT);
		this.treePane.setLayoutX(0);
		this.treePane.setLayoutY(0);
		this.treePane.prefWidthProperty().bind(this.separatorPane.layoutXProperty());
		this.treePane.prefHeightProperty().bind(this.heightProperty());
		this.treePane.getChildren().add(this.treeView);
		
		this.treePane.widthProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				ApplicationPreferences.treePanelSize = newValue.doubleValue();
			}
		});
	}
	
	private void createCanvasPane() {
		this.cvs.setWidth(this.project.getSettings().framewidth);
		this.cvs.setHeight(this.project.getSettings().frameheight);
		
		this.canvasPane = new Pane();
		this.canvasPane.setBackground(Urmusic.PANES_BACKGROUND_FLAT);
		this.canvasPane.layoutXProperty().bind(this.separatorPane.layoutXProperty().add(this.separatorPane.widthProperty()));
		this.canvasPane.setLayoutY(0);
		this.canvasPane.prefWidthProperty().bind(this.widthProperty().subtract(this.canvasPane.layoutXProperty()));
		this.canvasPane.prefHeightProperty().bind(this.heightProperty());
		this.canvasPane.getChildren().add(this.cvs);
		
		this.cvs.layoutXProperty().bind(this.canvasPane.widthProperty().divide(2).subtract(this.cvs.widthProperty().divide(2)));
		this.cvs.layoutYProperty().bind(this.canvasPane.heightProperty().divide(2).subtract(this.cvs.heightProperty().divide(2)));
		
		this.cvs.scaleXProperty().bind(Bindings.min(this.canvasPane.widthProperty().divide(this.cvs.widthProperty()), this.canvasPane.heightProperty().divide(this.cvs.heightProperty())));
		this.cvs.scaleYProperty().bind(this.cvs.scaleXProperty());
	}
	
	public Canvas getCanvas() {
		return this.cvs;
	}
	
	public Project getProject() {
		return this.project;
	}
}
