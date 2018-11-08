package com.example.demo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTube.Search;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Thumbnail;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Link;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SpringBootApplication
@RestController
public class DemoApplication {

	@RequestMapping("/")

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@SpringUI
	public class WebUI extends UI {

		/**
		 * YouTube Quick Search Tool - by Michael Ruiz 
		 */
		private static final long serialVersionUID = 7901512628763841010L;
		private YouTube youtube;
		private static final String PROPERTIES_FILENAME = "youtube.properties";
		private static final long NUMBER_OF_VIDEOS_RETURNED = 25;
		private TextField ta;
		private Button searchButton;
		private Button resetButton;
		private VerticalLayout mainLayout;
		private Panel subPanel;
		private HorizontalLayout bottomHl;
		private List<SearchResult> srchList;
		private HorizontalLayout hl;
		private VerticalLayout resultHolder = new VerticalLayout();
		private Boolean searchDone = false;
		private UrlDownload file;

		private void prettyPrint(Iterator<com.google.api.services.youtube.model.SearchResult> iterator, String query) {

			System.out.println("\n=============================================================");
			System.out.println("   First " + NUMBER_OF_VIDEOS_RETURNED + " videos for search on \"" + query + "\".");
			System.out.println("=============================================================\n");

			if (!iterator.hasNext()) {
				System.out.println(" There aren't any results for your query.");
			}

			while (iterator.hasNext()) {

				com.google.api.services.youtube.model.SearchResult singleVideo = iterator.next();
				ResourceId rId = singleVideo.getId();
				String link = "https://www.youtube.com/watch?v=" + rId.getVideoId();
				String linkHtml = "<a href=" + "\"" + link + "\"target=_blank>" + singleVideo.getSnippet().getTitle()
						+ "</a>";

				// Confirm that the result represents a video. Otherwise, the
				// item will not contain a video ID.
				if (rId.getKind().equals("youtube#video")) {

					subPanel = new Panel();
					bottomHl = new HorizontalLayout();
					resultHolder.addComponent(bottomHl);
					resultHolder.setMargin(false);
					bottomHl.addComponent(subPanel);

					Thumbnail thumbnail = singleVideo.getSnippet().getThumbnails().getDefault();
					ExternalResource res = new ExternalResource(thumbnail.getUrl());
					Image image = new Image(null, res);
					HorizontalLayout hol = new HorizontalLayout();
					Link vidLink = new Link(singleVideo.getSnippet().getTitle(), new ExternalResource(link));
					Button downloadButton = new Button();
					downloadButton.setIcon(VaadinIcons.DOWNLOAD);
					downloadButton.addClickListener(new ClickListener() {
						
						/**
						 * 
						 */
						private static final long serialVersionUID = 5460346696804686999L;

						@Override
						public void buttonClick(ClickEvent event) {
//							file = new UrlDownload(link, "test", "c:\\temp");
							
						}
					});

					vidLink.setTargetName("_blank");
					vidLink.setIcon(VaadinIcons.EXTERNAL_LINK);
					vidLink.setTargetHeight(400);
					vidLink.setTargetWidth(500);
					subPanel.setContent(hol);
					hol.addComponents(image, vidLink, downloadButton);

					System.out.println(" Video Id: " + rId.getVideoId());
					System.out.println(" Title: " + singleVideo.getSnippet().getTitle());
					System.out.println(" Thumbnail: " + thumbnail.getUrl());
					System.out.println(" Link: " + link);
					System.out.println(" HTML Link: " + linkHtml);
					System.out.println("\n-------------------------------------------------------------\n");
				}
			}
		}
		
		private void resetform(){
			resultHolder.removeAllComponents();
			srchList.clear();
		}
		
		private String getInputQuery() throws IOException {

			String query = ta.getValue();

			if (query.length() < 1) {
				query = "tacos";
			}

			return query;
		}

		@Override
		protected void init(VaadinRequest request) {

			Properties properties = new Properties();
			
			try {
				InputStream in = Search.class.getResourceAsStream("/" + PROPERTIES_FILENAME);
				properties.load(in);

			} catch (IOException e) {
				System.err.println("There was an error reading " + PROPERTIES_FILENAME + ": " + e.getCause() + " : "
						+ e.getMessage());
				System.exit(1);
			}

			try {
				youtube = new YouTube.Builder(new NetHttpTransport(),
						new com.google.api.client.json.jackson2.JacksonFactory(), new HttpRequestInitializer() {
							public void initialize(HttpRequest request) throws IOException {
							}
						}).setApplicationName("youtube-cmdline-search-sample").build();

			} catch (Exception e) {

				e.printStackTrace();
			}

			mainLayout = new VerticalLayout();
			hl = new HorizontalLayout();
			searchButton = new Button();
			resetButton = new Button();
			searchButton.setIcon(VaadinIcons.YOUTUBE);
			resetButton.setIcon(VaadinIcons.REFRESH);
			setContent(mainLayout);
			mainLayout.addComponents(hl, resultHolder);
			ta = new TextField();

			ShortcutListener scl = new ShortcutListener("Enter", KeyCode.ENTER, null) {

				/**
				 * 
				 */
				private static final long serialVersionUID = 373159605820217523L;

				@Override
				public void handleAction(Object sender, Object target) {
					searchButton.click();
				}
			};

			ta.addShortcutListener(scl);
			hl.addComponents(ta, searchButton, resetButton);
			
			resetButton.addClickListener(new ClickListener() {

				/**
				 * 
				 */
				private static final long serialVersionUID = -8722344029174864996L;

				@Override
				public void buttonClick(ClickEvent event) {
					resetform();	
				}
				
			});

			searchButton.addClickListener(new ClickListener() {

				/**
				 * 
				 */
				private static final long serialVersionUID = 4633676678733170967L;
				
				private List<com.google.api.services.youtube.model.SearchResult> searchResultList;				

				@Override
				public void buttonClick(ClickEvent event) {
					this.searchResultList = srchList;
					if(searchDone){
						resetform();						
					}
					try {
						String queryTerm = getInputQuery();
						YouTube.Search.List search = youtube.search().list("id,snippet");
						String apiKey = properties.getProperty("youtube.apikey");
						search.setKey(apiKey);
						search.setQ(queryTerm);
						search.setType("video");
						search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
						search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);
						SearchListResponse searchResponse = search.execute();
						srchList = searchResponse.getItems();
						if (srchList != null) {
							prettyPrint(srchList.iterator(), queryTerm);
						} 
						searchDone = true;

					} catch (IOException e) {						
						e.printStackTrace();
					} catch (Throwable t) {
						t.printStackTrace();
					}
				}
			});
		}
	}
}
