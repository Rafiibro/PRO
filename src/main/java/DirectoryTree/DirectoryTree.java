package DirectoryTree;

import Image.Image;
import SearchFilters.SearchFilter;
import interfaces.Filtrable;
import interfaces.Visitor;

import java.util.List;
import java.util.LinkedList;

public class DirectoryTree implements Filtrable {
	
	private List<DirectoryTree> children = new LinkedList<DirectoryTree>();
	private List<Image> images = new LinkedList<Image>();
	private SearchFilter isFiltred;
	private String name;
	
	public DirectoryTree() {
		name = "root";
		isFiltred = null;
	}
	
	public DirectoryTree(String name) {
		this.name = name;
		isFiltred = null;
	}
	
	public DirectoryTree addChild(String directoryName) {
		DirectoryTree temp = new DirectoryTree(directoryName);
		children.add(temp);
		return temp;
	}
	
	public void addImage(Image image) {
		images.add(image);
	}
	
	public boolean isFiltred() {
		if(isFiltred != null) {
			return true;
		}
		return false;
	}
	
	public void explore(Visitor visitor) {
		
		visitor.visit(this);
		
		for(Image i : images) {
			if(!i.isFiltred()) {
				visitor.visit(i);
			}
		}
		
		for(DirectoryTree d : children) {
			if(!d.isFiltred()) {
				d.explore(visitor);
			}
		}
		
	}
	
	public void Filtre(SearchFilter filter) {
		isFiltred = filter;
	}
	
	public void unFiltre(SearchFilter filter) {
		
		final SearchFilter temp = filter;
		
		explore(new Visitor() {
			
			public void visit(Filtrable f) {
				
				if(temp.equals(f.getFilter())) {
					f.unFiltre();
				}
				
			}
			
			
		});
	}
	
	public void unFiltre() {
		
		isFiltred = null;
		
	}
	
	public SearchFilter getFilter() {
		return isFiltred;
	}
	
	
	public String toString() {
		return name;
	}
	
	

}