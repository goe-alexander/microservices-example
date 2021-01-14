package license.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Organization implements Serializable {
    private static final long serialVersionUID = 7156526077883281623L;

    String id;
    String name;
    String contactName;
    String contactEmail;
    String contactPhone;
}
