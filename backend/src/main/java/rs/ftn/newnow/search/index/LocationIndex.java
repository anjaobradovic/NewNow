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
@Setting(replicas = 0, shards = 1)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationIndex {

    @Id
    private String id;

    /** UI-entered place name; full-text searchable, plus a keyword sub-field for future sort/exact use. */
    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword)
            }
    )
    private String name;

    @Field(type = FieldType.Text)
    private String description;

    /** Extracted PDF text. */
    @Field(type = FieldType.Text)
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

    @Field(type = FieldType.Keyword)
    private String imageUrl;

    @Field(type = FieldType.Boolean)
    private Boolean deleted;
}
