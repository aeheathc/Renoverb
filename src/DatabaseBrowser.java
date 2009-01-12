/* Renoverb is a program aimed at theater engineers that predicts the effect
   of sound absorbing materials on the reverberation of a room.
   Copyright 2008: Anthony Heathcoat, Nicholas Roth, Jim Simon, Yusuke Hasegawa

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;

/*
 * The database browser is the separate window with which you can search the database.
 */
class DatabaseBrowser extends JFrame implements ActionListener 
{
	public static final long serialVersionUID = 1;
	private JTable results = null;
	private JButton search = null, deleteMat = null, addMat = null;
	private JTextField name = null, manufacturer = null;
	
	public DatabaseBrowser()
	{
		//set basic window properties
		super("Database Browser");
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		setIconImage(Images.database().getImage());
		setResizable(true);
		setMinimumSize(new Dimension(600,200));
		setSize(650,400);
		
		//create result display area
		results = new JTable(new MatSearchTable());
		results.getTableHeader().setReorderingAllowed(false);
		JScrollPane resultScroll = new JScrollPane(results);
		
		//create search controls
		JPanel searchBar = new JPanel();
		searchBar.setLayout(new BoxLayout(searchBar,BoxLayout.X_AXIS));
		name = new JTextField();
		JPanel nameField = new JPanel();
		nameField.setLayout(new BoxLayout(nameField,BoxLayout.X_AXIS));
		nameField.add(new JLabel("Search Name:"));
		nameField.add(name);
		manufacturer = new JTextField();
		JPanel manuField = new JPanel();
		manuField.setLayout(new BoxLayout(manuField,BoxLayout.X_AXIS));
		manuField.add(new JLabel("Search Manufacturer:"));
		manuField.add(manufacturer);
		search = new JButton("Search");
		search.addActionListener(this);
		searchBar.add(nameField);
		searchBar.add(manuField);
		searchBar.add(search);
		
		//create selection controls
		JPanel commands = new JPanel();
		commands.setLayout(new BoxLayout(commands,BoxLayout.X_AXIS));
		deleteMat = new JButton("Delete selected");
		deleteMat.addActionListener(this);
		addMat = new JButton("Add selected to project");
		addMat.addActionListener(this);
		commands.add(addMat);
		commands.add(deleteMat);
		
		//throw it all together
		setLayout(new BorderLayout());
		add(searchBar,BorderLayout.NORTH);
		add(resultScroll,BorderLayout.CENTER);
		add(commands,BorderLayout.SOUTH);
		pack();
	}
	
	public void actionPerformed(ActionEvent e)
	{
		if(search.equals(e.getSource())) dbSearch();
		if(deleteMat.equals(e.getSource())) dbDelete();
		if(addMat.equals(e.getSource())) pick();
	}
	
	public void dbSearch()
	{
		MainWindow.database.setSearchName(name.getText());
		MainWindow.database.setSearchManufacturer(manufacturer.getText());
		for(int freq=0; freq<Renoverb.FREQUENCIES.length; freq++)
			MainWindow.database.setRange(Renoverb.FREQUENCIES[freq],0,1);
		LinkedList<Material> resultSet = MainWindow.database.customSearch();
		ListIterator<Material> iter = resultSet.listIterator();
		Object[][] newTable = new Object[resultSet.size()][Renoverb.FREQUENCIES.length+3];
		for(int row=0; iter.hasNext(); row++)
		{
			Material next = iter.next();
			newTable[row][0] = next.db_key;
			newTable[row][1] = next.name;
			newTable[row][2] = next.manufacturer;
			Iterator<Double> coeffs = next.getCoefficients().iterator();
			for(int column=3; coeffs.hasNext(); column++)
			{
				newTable[row][column] = coeffs.next();
			}
		}
		((MatSearchTable)(results.getModel())).setData(newTable);
	}

	private void dbDelete()
	{
		int[] selection = results.getSelectedRows();
		if(JOptionPane.CANCEL_OPTION == JOptionPane.showConfirmDialog(null, "Delete " + selection.length + " materials from database?\n(This is permanent.)", "Material Database", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE))
			return;
		for(int x=0; x<selection.length; x++)
			MainWindow.database.removeMaterial(((Integer)(results.getValueAt(selection[x], 0))).intValue());
		dbSearch();
	}
	
	private void pick()
	{
		//array of all the rows selected in the table
		int[] selection = results.getSelectedRows();
		//for each selected row, which represents a material...
		for(int x=0; x<selection.length; x++)
		{
			//make an array for all the absorption value for that material, one for each frequency
			double[] nextAbs = new double[Renoverb.FREQUENCIES.length];
			//For each frequency, the absorption value for that freq. equals (iterate through the columns of the CURRENT row [x] WITHIN the [selection])
			for(int y=0; y<Renoverb.FREQUENCIES.length; y++) nextAbs[y] = ((Double)(results.getValueAt(selection[x], y+3))).doubleValue();
			//add a material object represented by the current row
			MainWindow.matList.add(new MaterialTable((String)(results.getValueAt(selection[x], 1)),(String)(results.getValueAt(selection[x], 2)),nextAbs));
		}
		MainWindow.matList.validate();
	}
	
	public void setVisible(boolean visible)
	{
		super.setVisible(visible);
		dbSearch();
	}
	
	class MatSearchTable extends AbstractTableModel
	{
		public static final long serialVersionUID = 1;
	    private String[] columnNames = {"ID", "Name","Manufacturer","32","65","125","250","500","1000","2000","4000","8000"};
	    private Object[][] rowData = {{null,"","",null,null,null,null,null,null,null,null,null}};
	    public String getColumnName(int col)
	    {
	        return columnNames[col];
	    }
	    public int getRowCount() { return rowData.length; }
	    public int getColumnCount() { return columnNames.length; }
	    public Object getValueAt(int row, int col)
	    {
	        return rowData[row][col];
	    }
	    public boolean isCellEditable(int row, int col) {return false;}
	    public void setValueAt(Object value, int row, int col)
	    {
	        rowData[row][col] = value;
	        fireTableCellUpdated(row, col);
	    }
	    public Class<?> getColumnClass(int c)
	    {
	        if(c == 1 || c == 2) return "".getClass();
	        else return (new Double(0)).getClass();
	    }
	    public void setData(Object[][] newData)
	    {
	    	rowData = newData;
	    	fireTableDataChanged();
	    }
	}
}