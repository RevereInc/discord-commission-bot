package com.flux.discordbot.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Document(collection = "freelancers")
public class Freelancer {
    @Id
    @Field(name = "id")
    private String m_id;
    @Field(name = "user_id")
    private long m_userId;
    @Field(name = "name")
    private String m_name;
    @Field(name = "services")
    private List<Long> m_serviceRoleIds;
    @Field(name = "bio")
    private String m_bio;
    @Field(name = "title_descriptions")
    private List<TitleDescription> m_titleDescriptions;
}
