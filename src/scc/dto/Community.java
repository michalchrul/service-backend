package scc.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Community implements Serializable {

    private static final long serialVersionUID = -8369818167004609239L;

    String id;
    String name;
}
