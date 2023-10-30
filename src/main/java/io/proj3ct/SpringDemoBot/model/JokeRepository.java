package io.proj3ct.SpringDemoBot.model;

import org.springframework.data.repository.CrudRepository;

public interface JokeRepository extends CrudRepository<Joke, Integer> {
}
