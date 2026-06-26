package rs.ftn.newnow.search.index;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.time.Instant;

/**
 * The "Index Unit" for places. This is a denormalized projection of a Location,
 * kept in sync with the relational source-of-truth via SearchIndexService.
 *
 * Index settings: single-shard, replicas=0 so the cluster stays green on one node.
 */
@Document(indexName = "places")
@Setting(settingPath = "elasticsearch/place-index-settings.json")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationIndex {

    @Id
    private String id;

    /** UI-entered place name; full-text searchable through the custom case/script-folding analyzer. */
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "newnow_text", searchAnalyzer = "newnow_text"),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword)
            }
    )
    private String name;

    @Field(type = FieldType.Text, analyzer = "newnow_text", searchAnalyzer = "newnow_text")
    private String description;

    /** Extracted PDF text — same analyzer so Cyrillic-Latin/case folding applies. */
    @Field(type = FieldType.Text, analyzer = "newnow_text", searchAnalyzer = "newnow_text")
    private String pdfDescription;

    /** MinIO object key for the attached PDF, or null when none. */
    @Field(type = FieldType.Keyword)
    private String pdfObjectKey;

    @Field(type = FieldType.Date)
    private Instant pdfUploadedAt;

    /** Number of reviews counted by the canonical app definition (deleted=false AND deletedByManager=false). */
    @Field(type = FieldType.Integer)
    private Integer reviewCount;

    @Field(type = FieldType.Keyword)
    private String address;

    @Field(type = FieldType.Keyword)
    private String type;

    @Field(type = FieldType.Double)
    private Double totalRating;

    /** Average of the {@code performance} rating across the rating source. Null if no review rated it. */
    @Field(type = FieldType.Double)
    private Double avgPerformance;

    /** Combined sound + light (per K5's data model). Null if no review rated it. */
    @Field(type = FieldType.Double)
    private Double avgSoundAndLighting;

    /** Average of the {@code venue} (space) rating. Null if no review rated it. */
    @Field(type = FieldType.Double)
    private Double avgVenue;

    /** Average of {@code overallImpression}, read directly — not derived from other categories. */
    @Field(type = FieldType.Double)
    private Double avgOverallImpression;

    @Field(type = FieldType.Keyword)
    private String imageUrl;

    @Field(type = FieldType.Boolean)
    private Boolean deleted;
}
