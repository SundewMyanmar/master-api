package com.sdm.admin.service;

import com.sdm.admin.model.Role;
import com.sdm.admin.repository.RoleRepository;
import com.sdm.core.db.repository.DefaultRepository;
import com.sdm.core.model.response.PaginationResponse;
import com.sdm.core.service.ReactiveService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Log4j2
public class ReactiveRoleService  extends ReactiveService<Role,Integer> {
    @Autowired
    private RoleRepository repository;

    @Override
    protected DefaultRepository<Role, Integer> getRepository() {
        return repository;
    }

    public Mono<Role> getRoleById(Integer id){
        return this.findById(id);
    }



    public Flux<Role> findAll(){
        return this.findAll();
    }

    public Mono<ResponseEntity<Role>> getHttpRoleById(Integer id){
        return this.getRoleById(id).map(ResponseEntity::ok);
    }

    public Flux<ResponseEntity<Role>> getHttpFindAll(){
        return this.findAll().map(ResponseEntity::ok);
    }

    public Mono<ResponseEntity<PaginationResponse<Role>>> paging(Pageable page, String filter){
        return this.findAll(filter,page).map(ResponseEntity::ok);
    }

    public Mono<Role> save(Role role){
        return this.save(role);
    }
}
