package br.com.alura.forum.controller;

import java.net.URI;
import java.util.List;

import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.alura.forum.controller.dto.DetalhesDoTopicoDto;
import br.com.alura.forum.controller.dto.TopicoDto;
import br.com.alura.forum.controller.form.AtualizacaoTopicoForm;
import br.com.alura.forum.controller.form.TopicoForm;
import br.com.alura.forum.modelo.Curso;
import br.com.alura.forum.modelo.Topico;
import br.com.alura.forum.reposiory.CursoRepository;
import br.com.alura.forum.reposiory.TopicoRepository;

@RestController
@RequestMapping("/topicos")
public class TopicosController {
	
	@Autowired
	private  TopicoRepository topicoRepository;
	
	@Autowired
	private  CursoRepository cursoRepository;

	@GetMapping
	@Cacheable(value="listaDeTopicos")
	public Page<TopicoDto> lista(
			@RequestParam(required = false) String nomeCurso,
			@PageableDefault(sort ="id" , direction = Direction.DESC, page = 0, size = 10) Pageable paginacao){
		
		
		if(nomeCurso == null) {
			Page<Topico> topicos = topicoRepository.findAll(paginacao);
			List<TopicoDto> topicoList = TopicoDto.converter(topicos.getContent());
			return new PageImpl<>(topicoList, topicos.getPageable(), topicoList.size());
		}else {
			Page<Topico> topicos = topicoRepository.findByCursoNome(nomeCurso, paginacao);
			List<TopicoDto> topicoList = TopicoDto.converter(topicos.getContent());
			return new PageImpl<>(topicoList, topicos.getPageable(), topicoList.size());
		}
		
	}
	
	@PostMapping
	@Transactional
	@CacheEvict(value = "listaDeTopicos", allEntries = true)
	public ResponseEntity<TopicoDto> cadastrar(@RequestBody @Valid TopicoForm form, UriComponentsBuilder uriBuilder) {
		Curso curso =  cursoRepository.findByNome(form.getNomeCurso());
		Topico topico = new Topico(form.getTitulo(), form.getMensagem(), curso);
		topico = topicoRepository.save(topico);
		
		URI uri = uriBuilder.path("/topicos/{id}").buildAndExpand(topico.getId()).toUri();
		return ResponseEntity.created(uri).body(new TopicoDto(topico));
	}
	
	@GetMapping("/{id}")
	public DetalhesDoTopicoDto detalhar(@PathVariable Long id) {
		Topico topico = topicoRepository.findById(id)
				.orElseThrow();
		return new DetalhesDoTopicoDto(topico);
	}
	
	@PutMapping("/{id}")
	@Transactional
	@CacheEvict(value = "listaDeTopicos", allEntries = true)
	public ResponseEntity<TopicoDto> atualizar(@PathVariable Long id, @RequestBody @Valid AtualizacaoTopicoForm form) {
		Topico topico = topicoRepository.findById(id)
				.orElseThrow();
		
		topico.setTitulo(form.getTitulo());
		topico.setMensagem(form.getMensagem());
		topico = topicoRepository.save(topico);
		
		return ResponseEntity.ok(new TopicoDto(topico));
	}
	
	
	@DeleteMapping("/{id}")
	@Transactional
	@CacheEvict(value = "listaDeTopicos", allEntries = true)
	public ResponseEntity<?> remover(@PathVariable Long id) {
		topicoRepository.deleteById(id);
	
		return ResponseEntity.ok().build();
	}
}
