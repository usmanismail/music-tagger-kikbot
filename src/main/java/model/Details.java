package model;

import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = Inclusion.NON_DEFAULT)
public class Details
{

    @JsonProperty("release_date")
    private String releaseDate;
    private String label;
    @JsonProperty("external_metadata")
    private Map<String, ExternalData> externalData;
    private String title;
    @JsonProperty("duration_ms")
    private long duration;
    private List<Genre> genres;
    private Map<String, String> album;
    private List<Map<String, String>> artists;

}
