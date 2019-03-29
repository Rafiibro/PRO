package Image;
import interfaces.Filtrable;
import SearchFilters.SearchFilter;

public class Image implements Filtrable {

	private SearchFilter isFiltred;
	private String path;
	
	public String getPath(){
		return path;
	}
	
	public String toString() {
		return path;
	}
	
	
	public Image(String path) {
		this.path = path;
		
	}
	
	public boolean isFiltred() {
		if(isFiltred != null) {
			return true;
		}
		return false;
	}

	public void Filtre(SearchFilter filter) {
		isFiltred = filter;
		
	}

	public void unFiltre() {
		isFiltred = null;
		
	}

	public SearchFilter getFilter() {
		return isFiltred;
	}
	

}