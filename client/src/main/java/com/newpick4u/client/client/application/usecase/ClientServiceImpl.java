package com.newpick4u.client.client.application.usecase;

import com.newpick4u.client.client.application.dto.request.CreateClientRequestDto;
import com.newpick4u.client.client.application.dto.request.UpdateClientRequestDto;
import com.newpick4u.client.client.application.exception.ClientException.DuplicateEmailException;
import com.newpick4u.client.client.application.exception.ClientException.DuplicatePhoneNumberException;
import com.newpick4u.client.client.application.exception.ClientException.NotFoundException;
import com.newpick4u.client.client.domain.entity.Client;
import com.newpick4u.client.client.domain.repository.ClientRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j(topic = "ClientService")
@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

  private final ClientRepository clientRepository;

  @Override
  @Transactional
  public UUID saveClient(CreateClientRequestDto request) {
    validateCondition(request);
    Client client = Client.create(request.name(), request.industry(), request.email(),
        request.phone(),
        request.address());
    Client saveClient = clientRepository.save(client);
    return saveClient.getClientId();
  }

  @Override
  public UUID updateClient(UUID clientId, UpdateClientRequestDto request) {
    Client client = clientRepository.findById(clientId)
        .orElseThrow(NotFoundException::new);
    client.updateClient(request.name(), request.email(), request.phone(),
        request.address());
    Client updatedClient = clientRepository.save(client);
    return updatedClient.getClientId();
  }

  @Override
  public UUID deleteClient(UUID clientId, Long deletedBy) {
    Client client = clientRepository.findById(clientId)
        .orElseThrow(NotFoundException::new);
    client.delete(LocalDateTime.now(), deletedBy);
    Client deletedClient = clientRepository.save(client);
    return deletedClient.getClientId();
  }

  public void validateCondition(CreateClientRequestDto request) {
    if (clientRepository.existsByEmail(request.email())) {
      throw new DuplicateEmailException();
    }
    if (clientRepository.existsByPhone(request.phone())) {
      throw new DuplicatePhoneNumberException();
    }
  }
}
