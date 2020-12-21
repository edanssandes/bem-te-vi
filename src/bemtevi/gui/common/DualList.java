package bemtevi.gui.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import bemtevi.parsers.judiciario.trf1.ConstantesTRF1;


// Adaptado de ttp://www.java2s.com/Tutorials/Java/Swing/JList/Create_a_dual_list_control_with_JList_in_Java.htm
/**
 * Componente de seleção de elementos utilizando duas listas.
 * 
 * @author edans
 * 
 * @param <T>
 *            tipo de dados a ser apresentado.
 */
public class DualList<T> extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final String ADD_BUTTON_LABEL = ">";
	private static final String ADD_ALL_BUTTON_LABEL = ">>";
	private static final String REMOVE_BUTTON_LABEL = "<";
	private static final String REMOVE_ALL_BUTTON_LABEL = "<<";

	private static final String DEFAULT_SOURCE_CHOICE_LABEL = "Available Choices";
	private static final String DEFAULT_DEST_CHOICE_LABEL = "Your Choices";

	private JPanel sourcePanel;
	private JLabel sourceLabel;
	private JList<T> sourceList;
	private JScrollPane sourceScrollPane;
	private SortedListModel<T> sourceListModel;

	private JPanel destPanel;
	private JLabel destLabel;
	private JList<T> destList;
	private JScrollPane destScrollPane;
	private SortedListModel<T> destListModel;

	private JButton addButton;
	private JButton addAllButton;
	private JButton removeButton;
	private JButton removeAllButton;
	private JPanel buttonsPanel;
	private int disablePolicy;
	private List<T> stackedItems;

	/**
	 * Model contendo os dados apresentando na {@link DualList}.
	 * 
	 * @author edans
	 * 
	 * @param <T>
	 *            Tipo de dados da lista.
	 */
	class SortedListModel<T> extends AbstractListModel<T> {

		SortedSet<T> model;

		public SortedListModel() {
			model = new TreeSet<T>();
		}

		public List<T> getElements() {
			ArrayList<T> array = new ArrayList<T>();
			for (Object e : model.toArray()) {
				array.add((T) e);
			}
			return array;
		}

		public int getSize() {
			return model.size();
		}

		public T getElementAt(int index) {
			return (T) (model.toArray()[index]);
		}

		public void add(T element) {
			if (model.add(element)) {
				fireContentsChanged(this, 0, getSize());
			}
		}

		public void addAll(T elements[]) {
			addAll(Arrays.asList(elements));
		}

		public void addAll(Collection<T> elements) {
			model.addAll(elements);
			fireContentsChanged(this, 0, getSize());
		}

		public void clear() {
			model.clear();
			fireContentsChanged(this, 0, getSize());
		}

		public boolean contains(Object element) {
			return model.contains(element);
		}

		public Object firstElement() {
			return model.first();
		}

		public Iterator<T> iterator() {
			return model.iterator();
		}

		public Object lastElement() {
			return model.last();
		}

		public boolean removeElement(Object element) {
			boolean removed = model.remove(element);
			if (removed) {
				fireContentsChanged(this, 0, getSize());
			}
			return removed;
		}

	}

	/**
	 * LayoutManager utilizado para posicionar os componentes do DualList
	 * @author edans
	 */
	private class DualListLayoutManager implements LayoutManager {

		DualListLayoutManager() {
		}

		public void addLayoutComponent(String arg0, Component arg1) {
		}

		public void layoutContainer(Container parent) {
			Insets insets = getInsets();
			int maxWidth = getWidth() - (insets.left + insets.right);
			int maxHeight = getHeight() - (insets.top + insets.bottom);

			int ltWidth = (maxWidth - 40) / 2;
			int btWidth = maxWidth - ltWidth * 2;
			int lbHeight = sourceLabel.getPreferredSize().height;
			int btHeight = 20;
			sourcePanel.setBounds(insets.left, insets.top, ltWidth, maxHeight);
			buttonsPanel.setBounds(insets.left + ltWidth, insets.top + lbHeight
					+ ((maxHeight - lbHeight) - btHeight * 4) / 2, btWidth,
					btHeight * 4);
			destPanel.setBounds(insets.left + maxWidth - ltWidth, insets.top,
					ltWidth, maxHeight);
		}

		public Dimension minimumLayoutSize(Container arg0) {
			Dimension dimension = new Dimension();
			dimension.width += sourcePanel.getMinimumSize().width;
			dimension.width += buttonsPanel.getMinimumSize().width;
			dimension.width += destPanel.getMinimumSize().width;

			dimension.height = Math.max(dimension.height,
					sourcePanel.getMinimumSize().height);
			dimension.height = Math.max(dimension.height,
					buttonsPanel.getMinimumSize().height);
			dimension.height = Math.max(dimension.height,
					destPanel.getMinimumSize().height);

			Insets insets = getInsets();
			dimension.height += insets.top + insets.bottom;
			dimension.width += insets.left + insets.right;

			return dimension;
		}

		public Dimension preferredLayoutSize(Container arg0) {
			Dimension dimension = new Dimension();
			dimension.width += sourcePanel.getPreferredSize().width;
			dimension.width += buttonsPanel.getPreferredSize().width;
			dimension.width += destPanel.getPreferredSize().width;

			dimension.height = Math.max(dimension.height,
					sourcePanel.getPreferredSize().height);
			dimension.height = Math.max(dimension.height,
					buttonsPanel.getPreferredSize().height);
			dimension.height = Math.max(dimension.height,
					destPanel.getPreferredSize().height);

			Insets insets = getInsets();
			dimension.height += insets.top + insets.bottom;
			dimension.width += insets.left + insets.right;

			return dimension;
		}

		public void removeLayoutComponent(Component arg0) {
		}

	}

	public DualList() {
		initScreen();
	}

	public String getSourceChoicesTitle() {
		return sourceLabel.getText();
	}

	public void setSourceChoicesTitle(String newValue) {
		sourceLabel.setText(newValue);
	}

	public String getDestinationChoicesTitle() {
		return destLabel.getText();
	}

	public void setDestinationChoicesTitle(String newValue) {
		destLabel.setText(newValue);
	}

	public List<T> getSelectedItems() {
		return destListModel.getElements();
	}

	public void setItems(Collection<T> items) {
		sourceListModel.clear();
		destListModel.clear();
		sourceListModel.addAll(items);
	}

	public void setSelectedItems(Collection<T> items) {
		deselectAllItems();
		selectItems(items);
		stackedItems = null;
	}

	public void clear() {
		if (disablePolicy == 2) {
			selectAllItems();
		} else {
			deselectAllItems();
		}
		stackedItems = null;
	}

	public void selectItems(Collection<T> items) {
		List<T> existantItems = new ArrayList<T>();
		for (T i : items) {
			if (sourceListModel.contains(i)) {
				existantItems.add(i);
			}
		}
		removeSourceElements(existantItems);
		addDestinationElements(existantItems);
	}

	public void deselectItems(Collection<T> items) {
		List<T> existantItems = new ArrayList<T>();
		for (T i : items) {
			if (destListModel.contains(i)) {
				existantItems.add(i);
			}
		}
		removeDestinationElements(existantItems);
		addSourceElements(existantItems);
	}

	@Override
	public void setFont(Font f) {
		super.setFont(f);
		if (destList != null) {
			destList.setFont(f);
		}
		if (sourceList != null) {
			sourceList.setFont(f);
		}
	}

	public void setVisibleRowCount(int newValue) {
		sourceList.setVisibleRowCount(newValue);
		destList.setVisibleRowCount(newValue);
	}

	public int getVisibleRowCount() {
		return sourceList.getVisibleRowCount();
	}

	public void setSelectionBackground(Color newValue) {
		sourceList.setSelectionBackground(newValue);
		destList.setSelectionBackground(newValue);
	}

	public Color getSelectionBackground() {
		return sourceList.getSelectionBackground();
	}

	public void setSelectionForeground(Color newValue) {
		sourceList.setSelectionForeground(newValue);
		destList.setSelectionForeground(newValue);
	}

	public Color getSelectionForeground() {
		return sourceList.getSelectionForeground();
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		sourceList.setEnabled(enabled);
		destList.setEnabled(enabled);
		sourceLabel.setEnabled(enabled);
		destLabel.setEnabled(enabled);
		addButton.setEnabled(enabled);
		removeButton.setEnabled(enabled);
		addAllButton.setEnabled(enabled);
		removeAllButton.setEnabled(enabled);
		sourceScrollPane.setEnabled(enabled);
		destScrollPane.setEnabled(enabled);

		if (disablePolicy != 0) {
			if (enabled) {
				if (stackedItems != null) {
					setSelectedItems(stackedItems);
					stackedItems = null;
				}
			} else {
				if (stackedItems == null) {
					stackedItems = getSelectedItems();
					if (disablePolicy == 1) {
						deselectAllItems();
					} else {
						selectAllItems();
					}
				}
			}
		}
	}

	private void addSelected() {
		List<T> items = sourceList.getSelectedValuesList();
		addDestinationElements(items);
		removeSourceElements(items);
	}

	private void removeSelected() {
		List<T> items = destList.getSelectedValuesList();
		addSourceElements(items);
		removeDestinationElements(items);
	}

	private void selectAllItems() {
		List<T> items = sourceListModel.getElements();
		addDestinationElements(items);
		removeSourceElements(items);
	}

	private void deselectAllItems() {
		List<T> items = destListModel.getElements();
		addSourceElements(items);
		removeDestinationElements(items);
	}

	private void addSourceElements(Collection<T> newValue) {
		sourceListModel.addAll(newValue);
		sourceList.getSelectionModel().clearSelection();
	}

	private void addDestinationElements(Collection<T> newValue) {
		destListModel.addAll(newValue);
		destList.getSelectionModel().clearSelection();
	}

	private void removeSourceElements(Collection<T> items) {
		for (T sel : items) {
			sourceListModel.removeElement(sel);
		}
		sourceList.getSelectionModel().clearSelection();
	}

	private void removeDestinationElements(Collection<T> items) {
		for (T sel : items) {
			destListModel.removeElement(sel);
		}
		destList.getSelectionModel().clearSelection();
	}

	private void initScreen() {
		setBorder(BorderFactory.createEtchedBorder());
		// setBackground(Color.yellow);
		setLayout(new DualListLayoutManager());
		sourceLabel = new JLabel(DEFAULT_SOURCE_CHOICE_LABEL);
		sourceListModel = new SortedListModel();
		sourceList = new JList(sourceListModel);
		sourceScrollPane = new JScrollPane(sourceList);

		addButton = new JButton(ADD_BUTTON_LABEL);
		addButton.addActionListener(new AddListener());

		removeButton = new JButton(REMOVE_BUTTON_LABEL);
		removeButton.addActionListener(new RemoveListener());

		addAllButton = new JButton(ADD_ALL_BUTTON_LABEL);
		addAllButton.addActionListener(new AddAllListener());

		removeAllButton = new JButton(REMOVE_ALL_BUTTON_LABEL);
		removeAllButton.addActionListener(new RemoveAllListener());

		destLabel = new JLabel(DEFAULT_DEST_CHOICE_LABEL);
		destListModel = new SortedListModel();
		destList = new JList(destListModel);
		destScrollPane = new JScrollPane(destList);

		destList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					removeSelected();
				}
			}
		});
		sourceList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					addSelected();
				}
			}
		});

		destScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		sourceScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		/*
		 * sourceLabel.setMinimumSize(new Dimension(0, sourceLabel
		 * .getPreferredSize().height)); destLabel.setMinimumSize(new
		 * Dimension(0, destLabel.getPreferredSize().height));
		 * 
		 * sourceLabel.setPreferredSize(new Dimension(10, sourceLabel
		 * .getPreferredSize().height)); destLabel.setPreferredSize(new
		 * Dimension(10, destLabel .getPreferredSize().height));
		 * 
		 * destScrollPane.setMinimumSize(new Dimension(0, destScrollPane
		 * .getPreferredSize().height)); sourceScrollPane.setMinimumSize(new
		 * Dimension(0, sourceScrollPane .getPreferredSize().height));
		 */

		Font buttonFont = addButton.getFont().deriveFont(9.0f);
		Border border = BorderFactory.createEtchedBorder(1);
		Dimension buttonDim = new Dimension(40, 20);
		addButton.setPreferredSize(buttonDim);
		addButton.setMinimumSize(buttonDim);
		addButton.setMaximumSize(buttonDim);
		addButton.setFont(buttonFont);
		addButton.setBorder(border);
		removeButton.setPreferredSize(buttonDim);
		removeButton.setMinimumSize(buttonDim);
		removeButton.setMaximumSize(buttonDim);
		removeButton.setFont(buttonFont);
		removeButton.setBorder(border);
		addAllButton.setPreferredSize(buttonDim);
		addAllButton.setMinimumSize(buttonDim);
		addAllButton.setMaximumSize(buttonDim);
		addAllButton.setFont(buttonFont);
		addAllButton.setBorder(border);
		removeAllButton.setPreferredSize(buttonDim);
		removeAllButton.setMinimumSize(buttonDim);
		removeAllButton.setMaximumSize(buttonDim);
		removeAllButton.setFont(buttonFont);
		removeAllButton.setBorder(border);

		buttonsPanel = new JPanel(new GridLayout(4, 1));
		buttonsPanel.add(addAllButton);
		buttonsPanel.add(addButton);
		buttonsPanel.add(removeButton);
		buttonsPanel.add(removeAllButton);

		destPanel = new JPanel(new BorderLayout());
		destPanel.add(destLabel, BorderLayout.NORTH);
		destPanel.add(destScrollPane, BorderLayout.CENTER);

		sourcePanel = new JPanel(new BorderLayout());
		sourcePanel.add(sourceLabel, BorderLayout.NORTH);
		sourcePanel.add(sourceScrollPane, BorderLayout.CENTER);

		add(sourcePanel);
		add(buttonsPanel);
		add(destPanel);
	}

	private class AddListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			addSelected();
		}
	}

	private class RemoveListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			removeSelected();
		}
	}

	private class AddAllListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			selectAllItems();
		}
	}

	private class RemoveAllListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			deselectAllItems();
		}
	}

	public void setDisablePolicy(int disablePolicy) {
		this.disablePolicy = disablePolicy;
	}

	public static void main(String args[]) {
		Set<String> names = ConstantesTRF1.getNomeOrgaos();
		JFrame f = new JFrame("Dual List Box Tester");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		DualList dual = new DualList();
		dual.setSourceChoicesTitle("Órgãos Inválidos");
		dual.setDestinationChoicesTitle("Órgãos Válidos");
		dual.setFont(dual.getFont().deriveFont(10.0f));
		dual.addDestinationElements(names);
		/*
		 * dual.addSourceElements(new String[] { "One", "Two", "Three" });
		 * dual.addSourceElements(new String[] { "Four", "Five", "Six" });
		 * dual.addSourceElements(new String[] { "Seven", "Eight", "Nine" });
		 * dual.addSourceElements(new String[] { "Ten", "Eleven", "Twelve" });
		 * dual.addSourceElements(new String[] { "Thirteen", "Fourteen",
		 * "Fifteen" }); dual.addSourceElements(new String[] { "Sixteen",
		 * "Seventeen", "Eighteen" }); dual.addSourceElements(new String[] {
		 * "Nineteen", "Twenty", "Thirty" });
		 */
		f.getContentPane().add(dual, BorderLayout.CENTER);
		f.setSize(300, 200);
		f.setVisible(true);
	}
}
