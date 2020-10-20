package scc.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.cache.annotation.Cacheable;

import java.io.Serializable;

@Cacheable(value="post")
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Post implements Serializable {

    private static final long serialVersionUID = 7156526077883281723L;

    String Id;
    String familyId;
    String title;
    String communityId;
    String creatorNickname;
    Long timeOfCreation;
    String message;
    String linkToImage;
    String linkToParentPost;
    Integer numberOfLikes;
}
