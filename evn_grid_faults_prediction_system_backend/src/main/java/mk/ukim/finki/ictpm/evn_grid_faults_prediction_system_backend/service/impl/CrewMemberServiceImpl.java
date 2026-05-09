package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.impl;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.CrewMemberResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.AddCrewMemberRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.exception.ConflictException;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.exception.ResourceNotFoundException;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.helpers.CrewMemberMapper;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.Crew;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.CrewMember;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.User;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.CrewMemberRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.CrewRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.UserRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.CrewMemberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CrewMemberServiceImpl implements CrewMemberService {

    private final CrewRepository crewRepository;
    private final CrewMemberRepository crewMemberRepository;
    private final UserRepository userRepository;
    private final CrewMemberMapper crewMemberMapper;

    public CrewMemberServiceImpl(CrewRepository crewRepository,
                                 CrewMemberRepository crewMemberRepository,
                                 UserRepository userRepository,
                                 CrewMemberMapper crewMemberMapper) {
        this.crewRepository = crewRepository;
        this.crewMemberRepository = crewMemberRepository;
        this.userRepository = userRepository;
        this.crewMemberMapper = crewMemberMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CrewMemberResponse> findByCrewId(Long crewId) {
        if (!crewRepository.existsById(crewId)) {
            throw new ResourceNotFoundException("Crew with id " + crewId + " not found");
        }
        return crewMemberRepository.findByCrewId(crewId).stream()
                .map(crewMemberMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CrewMemberResponse findByUserId(Long userId) {
        CrewMember member = crewMemberRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Crew member with user id " + userId + " not found"));
        return crewMemberMapper.toResponse(member);
    }

    @Override
    public CrewMemberResponse addMemberToCrew(Long crewId, AddCrewMemberRequest request) {
        Crew crew = crewRepository.findById(crewId)
                .orElseThrow(() -> new ResourceNotFoundException("Crew with id " + crewId + " not found"));

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + request.userId() + " not found"));

        if (crewMemberRepository.findByUserId(request.userId()).isPresent()) {
            throw new ConflictException("User is already a member of a crew");
        }

        CrewMember member = new CrewMember();
        member.setCrew(crew);
        member.setUser(user);
        member.setFirstName(user.getFirstName());
        member.setLastName(user.getLastName());
        member.setPosition(request.position());

        CrewMember savedMember = crewMemberRepository.save(member);
        return crewMemberMapper.toResponse(savedMember);
    }

    @Override
    public void removeMemberFromCrew(Long crewId, Long memberId) {
        Crew crew = crewRepository.findById(crewId)
                .orElseThrow(() -> new ResourceNotFoundException("Crew with id " + crewId + " not found"));

        CrewMember member = crewMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Crew member with id " + memberId + " not found"));

        if (!member.getCrew().getId().equals(crewId)) {
            throw new ConflictException("Crew member with id " + memberId + " does not belong to crew with id " + crewId);
        }

        crewMemberRepository.delete(member);
    }
}
