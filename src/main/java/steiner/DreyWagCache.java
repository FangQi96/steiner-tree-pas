package steiner;

public class DreyWagCache {
	private Integer cachedWeight;
	private Integer row;
	private Integer column;
	private Integer firstParentRow;
	private Integer secondParentRow;
	private Integer parentColumn;
	
	public DreyWagCache(Integer cachedWeight, Integer row, Integer column, Integer firstParentRow, Integer secondParentRow, Integer parentColumn) {
		set(cachedWeight, row, column, firstParentRow, secondParentRow, parentColumn);
	}
	
	public void set(Integer cachedWeight, Integer row, Integer column, Integer firstParentRow, Integer secondParentRow, Integer parentColumn) {
		setCachedWeight(cachedWeight);
		setRow(row);
		setColumn(column);
		setFirstParentRow(firstParentRow);
		setSecondParentRow(secondParentRow);
		setParentColumn(parentColumn);
	}

	public Integer getCachedWeight() {
		return cachedWeight;
	}

	private void setCachedWeight(Integer cachedWeight) {
		this.cachedWeight = cachedWeight;
	}

	public Integer getRow() {
		return row;
	}

	private void setRow(Integer row) {
		this.row = row;
	}

	public Integer getColumn() {
		return column;
	}

	private void setColumn(Integer column) {
		this.column = column;
	}

	public Integer getFirstParentRow() {
		return firstParentRow;
	}

	private void setFirstParentRow(Integer firstParentRow) {
		this.firstParentRow = firstParentRow;
	}

	public Integer getSecondParentRow() {
		return secondParentRow;
	}

	private void setSecondParentRow(Integer secondParentRow) {
		this.secondParentRow = secondParentRow;
	}

	public Integer getParentColumn() {
		return parentColumn;
	}

	private void setParentColumn(Integer parentColumn) {
		this.parentColumn = parentColumn;
	}
}
