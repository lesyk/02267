package tg.structures;

import javax.xml.bind.annotation.*;


@XmlRootElement(name = "link")
public class Link {
	
	private String uri;
	private String rel;
	
	public Link() {
		uri = ""; rel="";
	}
	
	@XmlAttribute(name="uri")
	public String getUri() {
		return uri;
	}
	
	public void setUri(String uri) {
		this.uri = uri;
	}
	
	@XmlAttribute(name="rel")
	public String getRel() {
		return rel;
	}
	
	public void setRel(String rel) {
		this.rel = rel;
	}
}
