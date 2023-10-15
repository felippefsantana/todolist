package br.com.felippefsantana.todolist.task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.felippefsantana.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/tasks")
public class TaskController {
  
  @Autowired
  private ITaskRepository taskRepository;

  @PostMapping("/")
  public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
    var userId = request.getAttribute("userId");
    taskModel.setUserId((UUID) userId);

    var currentDate = LocalDateTime.now();
    if (currentDate.isAfter(taskModel.getStartAt())) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The start date must be greater than the current date!");
    }
    if (currentDate.isAfter(taskModel.getEndAt())) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The end date must be greater than the current date!");
    }
    if (taskModel.getStartAt().isAfter(taskModel.getEndAt())) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The end date must be greater than the start date!");
    }

    var task = this.taskRepository.save(taskModel);
    return ResponseEntity.status(HttpStatus.CREATED).body(task);
  }

  @GetMapping("/")
  public List<TaskModel> list(HttpServletRequest request) {
    var userId = request.getAttribute("userId");
    var tasks = this.taskRepository.findByUserId((UUID) userId);
    return tasks;
  }

  @PutMapping("/{id}")
  public ResponseEntity update(@RequestBody TaskModel taskModel, HttpServletRequest request, @PathVariable UUID id) {
    var task  = this.taskRepository.findById(id).orElse(null);

    if (task == null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Task not found!");
    }
    
    var userId = request.getAttribute("userId");
    if (!task.getUserId().equals(userId)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not allowed to change this task!");
    }

    Utils.copyNonNullProperties(taskModel, task);

    var updatedTask = this.taskRepository.save(task);

    return ResponseEntity.status(HttpStatus.OK).body(updatedTask);
  }
}
