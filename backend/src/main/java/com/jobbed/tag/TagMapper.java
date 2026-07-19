package com.jobbed.tag;

import com.jobbed.tag.dto.TagResponse;
import org.springframework.stereotype.Component;

@Component
public class TagMapper {

    public TagResponse toResponse(Tag tag) {
        return new TagResponse(tag.getId(), tag.getName(), tag.getColor());
    }
}
